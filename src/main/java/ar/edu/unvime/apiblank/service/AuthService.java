package ar.edu.unvime.apiblank.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ar.edu.unvime.apiblank.domain.Usuario;
import ar.edu.unvime.apiblank.dto.usuario.LoginRequestDTO;
import ar.edu.unvime.apiblank.dto.usuario.LoginResponseDTO;
import ar.edu.unvime.apiblank.dto.usuario.RegistroRequestDTO;
import ar.edu.unvime.apiblank.dto.usuario.UsuarioResponseDTO;
import ar.edu.unvime.apiblank.exception.CredencialesInvalidasException;
import ar.edu.unvime.apiblank.exception.EmailYaRegistradoException;
import ar.edu.unvime.apiblank.mapper.UsuarioMapper;
import ar.edu.unvime.apiblank.repository.UsuarioRepository;
import ar.edu.unvime.apiblank.security.JwtService;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            UsuarioMapper usuarioMapper,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UsuarioResponseDTO registrar(RegistroRequestDTO request) {
        // 1. Verificar si el email ya existe (409 Conflict si ya está registrado)
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new EmailYaRegistradoException("El email ya se encuentra registrado: " + request.email());
        }

        // 2. Hashear la contraseña con PasswordEncoder (nunca en texto plano)
        String passwordHash = passwordEncoder.encode(request.password());

        // 3. Crear el usuario de dominio con rol "USER" por defecto
        Usuario nuevoUsuario = new Usuario(
                null,
                request.email(),
                passwordHash,
                "USER"
        );

        // 4. Guardar y devolver DTO de respuesta (sin exponer el hash de la contraseña)
        Usuario guardado = usuarioRepository.save(nuevoUsuario);
        return usuarioMapper.toDTO(guardado);
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        // 1. Buscar usuario por email (401 si no existe)
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new CredencialesInvalidasException("Email o contraseña incorrectos"));

        // 2. Validar contraseña contra el hash guardado (401 si no coincide)
        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException("Email o contraseña incorrectos");
        }

        // 3. Generar JWT firmado con el id y el rol del usuario adentro
        String token = jwtService.generarToken(usuario.getId(), usuario.getRol());
        return new LoginResponseDTO(token);
    }
}
