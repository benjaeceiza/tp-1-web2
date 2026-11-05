package ar.edu.unvime.apiblank.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ar.edu.unvime.apiblank.dto.usuario.LoginRequestDTO;
import ar.edu.unvime.apiblank.dto.usuario.LoginResponseDTO;
import ar.edu.unvime.apiblank.dto.usuario.RegistroRequestDTO;
import ar.edu.unvime.apiblank.dto.usuario.UsuarioResponseDTO;
import ar.edu.unvime.apiblank.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints públicos para registro de nuevos usuarios e inicio de sesión con emisión de token JWT")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Registrar un nuevo usuario",
            description = "Endpoint público. Registra un usuario en la base de datos hasheando su contraseña con BCrypt y asignándole el rol USER por defecto."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (email con formato inválido o contraseña vacía)"),
            @ApiResponse(responseCode = "409", description = "Conflicto: el email ya se encuentra registrado")
    })
    public UsuarioResponseDTO registrar(@Valid @RequestBody RegistroRequestDTO request) {
        return authService.registrar(request);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión y obtener token JWT",
            description = "Endpoint público. Valida email y contraseña contra el hash BCrypt almacenado y devuelve un token JWT firmado con el id y el rol del usuario."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa, devuelve el token JWT"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas (email inexistente o contraseña incorrecta)")
    })
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }
}
