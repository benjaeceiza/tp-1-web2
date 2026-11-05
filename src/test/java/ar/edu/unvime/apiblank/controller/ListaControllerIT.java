package ar.edu.unvime.apiblank.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.TimeZone;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.unvime.apiblank.domain.Usuario;
import ar.edu.unvime.apiblank.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ListaControllerIT {

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
    }

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16"));

    @DynamicPropertySource
    static void propiedadesDeConexion(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void login_conCredencialesValidas_devuelve200ConToken() throws Exception {
        registrarUsuario("login.valido@test.com", "12345678");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "login.valido@test.com", "password": "12345678"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_conCredencialesInvalidas_devuelve401() throws Exception {
        registrarUsuario("login.invalido@test.com", "12345678");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "login.invalido@test.com", "password": "clave-erronea"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accederAListas_sinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/api/listas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accederAListaPropia_conTokenDelDueno_devuelve200() throws Exception {
        String tokenAna = registrarYLoguear("ana.propia@test.com", "12345678");
        long listaId = crearLista(tokenAna, "Lista propia de Ana");

        mockMvc.perform(get("/api/listas/{id}", listaId)
                        .header("Authorization", "Bearer " + tokenAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listaId))
                .andExpect(jsonPath("$.nombre").value("Lista propia de Ana"));
    }

    @Test
    void accederAListaAjena_conTokenDeOtroUsuario_devuelve403() throws Exception {
        String tokenAna = registrarYLoguear("ana.duena@test.com", "12345678");
        long listaId = crearLista(tokenAna, "Lista privada de Ana");
        String tokenBob = registrarYLoguear("bob.intruso@test.com", "12345678");

        mockMvc.perform(get("/api/listas/{id}", listaId)
                        .header("Authorization", "Bearer " + tokenBob))
                .andExpect(status().isForbidden());
    }

    @Test
    void accederAListaAjena_conRolAdmin_devuelve200() throws Exception {
        String tokenAna = registrarYLoguear("ana.para.admin@test.com", "12345678");
        long listaId = crearLista(tokenAna, "Lista de Ana vista por Admin");

        registrarUsuario("admin@test.com", "12345678");
        Usuario admin = usuarioRepository.findByEmail("admin@test.com").orElseThrow();
        admin.setRol("ADMIN");
        usuarioRepository.save(admin);

        String tokenAdmin = loguear("admin@test.com", "12345678");

        mockMvc.perform(get("/api/listas/{id}", listaId)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listaId))
                .andExpect(jsonPath("$.nombre").value("Lista de Ana vista por Admin"));
    }

    private void registrarUsuario(String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(email, password)))
                .andExpect(status().isCreated());
    }

    private String loguear(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = json.get("token").asText();
        assertThat(token).isNotBlank();
        return token;
    }

    private String registrarYLoguear(String email, String password) throws Exception {
        registrarUsuario(email, password);
        return loguear(email, password);
    }

    private long crearLista(String token, String nombre) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/listas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "%s"}
                                """.formatted(nombre)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }
}
