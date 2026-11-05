package ar.edu.unvime.apiblank.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ar.edu.unvime.apiblank.dto.usuario.LoginRequestDTO;
import ar.edu.unvime.apiblank.dto.usuario.LoginResponseDTO;
import ar.edu.unvime.apiblank.dto.usuario.RegistroRequestDTO;
import ar.edu.unvime.apiblank.dto.usuario.UsuarioResponseDTO;
import ar.edu.unvime.apiblank.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Operaciones para registro e inicio de sesión de usuarios")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un nuevo usuario", description = "Registra un usuario con contraseña hasheada y rol USER por defecto. Retorna 409 si el email ya existe.")
    public UsuarioResponseDTO registrar(@Valid @RequestBody RegistroRequestDTO request) {
        return authService.registrar(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Valida las credenciales del usuario y devuelve un token JWT firmado con su ID y rol. Retorna 401 si las credenciales son inválidas.")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }
}
