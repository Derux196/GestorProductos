package com.devsenior.gestorproductos;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.jayway.jsonpath.JsonPath;
import com.devsenior.gestorproductos.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAuthorizationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

        @Autowired
        private JdbcTemplate jdbcTemplate;

    @Test
    void catalogReadsArePublic() throws Exception {
        mockMvc.perform(get("/api/productos")).andExpect(status().isOk());
        mockMvc.perform(get("/api/categorias")).andExpect(status().isOk());
        mockMvc.perform(get("/api/marcas")).andExpect(status().isOk());
    }

        @Test
        void swaggerDocumentationIsPublic() throws Exception {
                mockMvc.perform(get("/v3/api-docs"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
                mockMvc.perform(get("/swagger-ui/index.html"))
                                .andExpect(status().isOk());
        }

    @Test
    void categoryWritesRequireAdmin() throws Exception {
        String body = "{\"name\":\"Cuidado personal\"}";

        mockMvc.perform(post("/api/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/categorias")
                .with(user("cliente").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/categorias")
                .with(user("administrador").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void brandWritesRequireAdmin() throws Exception {
        String body = "{\"name\":\"Marca de prueba\"}";

        mockMvc.perform(post("/api/marcas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/marcas")
                .with(user("cliente").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/marcas")
                .with(user("administrador").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void registrationHashesPasswordAndIssuesWorkingUserToken() throws Exception {
        String email = UUID.randomUUID() + "@tienda.test";
        String password = "clave-segura-123";
        String registerBody = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";

        MvcResult registration = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody))
                .andExpect(status().isCreated())
                .andReturn();

        String storedPassword = userRepository.findByEmail(email).orElseThrow().getPassword();
        assertTrue(storedPassword.startsWith("$2"));
        assertTrue(!storedPassword.equals(password));

        String userToken = JsonPath.read(registration.getResponse().getContentAsString(), "$.token");
        mockMvc.perform(get("/api/productos")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/categorias")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"No autorizado\"}"))
                .andExpect(status().isForbidden());

        jdbcTemplate.update("UPDATE usuarios SET role = 'ADMIN' WHERE email = ?", email);
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody))
                .andExpect(status().isOk())
                .andReturn();
        String adminToken = JsonPath.read(login.getResponse().getContentAsString(), "$.token");

        mockMvc.perform(post("/api/categorias")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Permitido\"}"))
                .andExpect(status().isCreated());
    }
}