package ar.edu.unvime.apiblank.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import ar.edu.unvime.apiblank.domain.Usuario;
import ar.edu.unvime.apiblank.dto.usuario.LoginRequestDTO;
import ar.edu.unvime.apiblank.dto.usuario.LoginResponseDTO;
import ar.edu.unvime.apiblank.dto.usuario.RegistroRequestDTO;
import ar.edu.unvime.apiblank.exception.CredencialesInvalidasException;
import ar.edu.unvime.apiblank.exception.EmailYaRegistradoException;
import ar.edu.unvime.apiblank.mapper.UsuarioMapper;
import ar.edu.unvime.apiblank.repository.UsuarioRepository;
import ar.edu.unvime.apiblank.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private JwtService jwtService;

    @Captor
    private ArgumentCaptor<Usuario> usuarioCaptor;

    private PasswordEncoder passwordEncoder;
    private AuthService service;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        service = new AuthService(repository, new UsuarioMapper(), passwordEncoder, jwtService);
    }

    @Test
    void registrar_nuncaGuardaPasswordEnTextoPlano() {
        when(repository.existsByEmail("ana@test.com")).thenReturn(false);
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        service.registrar(new RegistroRequestDTO("ana@test.com", "12345678"));

        verify(repository).save(usuarioCaptor.capture());
        Usuario guardado = usuarioCaptor.getValue();
        assertThat(guardado.getPasswordHash()).isNotEqualTo("12345678");
        assertThat(passwordEncoder.matches("12345678", guardado.getPasswordHash())).isTrue();
        assertThat(guardado.getRol()).isEqualTo("USER");
    }

    @Test
    void registrar_emailDuplicado_lanzaEmailYaRegistradoException() {
        when(repository.existsByEmail("ana@test.com")).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(new RegistroRequestDTO("ana@test.com", "12345678")))
                .isInstanceOf(EmailYaRegistradoException.class);
    }

    @Test
    void login_credencialesValidas_devuelveToken() {
        String hash = passwordEncoder.encode("12345678");
        Usuario usuario = new Usuario(1L, "ana@test.com", hash, "USER");
        when(repository.findByEmail("ana@test.com")).thenReturn(Optional.of(usuario));
        when(jwtService.generarToken(1L, "USER")).thenReturn("jwt-firmado-mock");

        LoginResponseDTO response = service.login(new LoginRequestDTO("ana@test.com", "12345678"));

        assertThat(response.token()).isEqualTo("jwt-firmado-mock");
    }

    @Test
    void login_passwordIncorrecta_lanzaCredencialesInvalidas() {
        String hash = passwordEncoder.encode("12345678");
        Usuario usuario = new Usuario(1L, "ana@test.com", hash, "USER");
        when(repository.findByEmail("ana@test.com")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> service.login(new LoginRequestDTO("ana@test.com", "clave-incorrecta")))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void login_emailInexistente_lanzaCredencialesInvalidas() {
        when(repository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequestDTO("noexiste@test.com", "12345678")))
                .isInstanceOf(CredencialesInvalidasException.class);
    }
}
