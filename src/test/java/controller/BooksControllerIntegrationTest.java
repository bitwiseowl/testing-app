package controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import main.CrudAppApplication;
import models.Books;
import models.BooksDTO;
import models.BookUpdateDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(classes = CrudAppApplication.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // clean DB between tests
class BooksControllerIntegrationTest {
	
	// To run this test the database should be  empty
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    //Create a book
    @WithMockUser
    @Test
    void shouldCreateBook() throws Exception {
        BooksDTO dto = new BooksDTO("Alice Author", "Integration Book", 15.99, "123");

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.isbn").value("123"))
                .andExpect(jsonPath("$.title").value("Integration Book"));
    }

    //Read
    @WithMockUser
    @Test
    void shouldReturnBooksList() throws Exception {
        // first insert a book
        BooksDTO dto = new BooksDTO("Author Name", "Test Book", 20.00, "000");
        
        
        //delete any book with isbn 000 if it already exists
        mockMvc.perform(delete("/books/000"));
        
        //posting book
        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isAccepted());
        
        MvcResult result = mockMvc.perform(get("/books")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
        
        String json = result.getResponse().getContentAsString();
        
        List<Books> books = objectMapper.readValue(
                json, new TypeReference<List<Books>>() {});
        
        assertThat(books).isNotEmpty();

    }

    //Modify
    @WithMockUser
    @Test
    void shouldUpdateBookTitle() throws Exception {
        // insert a book first
        BooksDTO dto = new BooksDTO("Charlie Author", "Old Title", 30.00,  "333" );

        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isAccepted());

        // update title
        BookUpdateDTO updateDTO = new BookUpdateDTO();
        updateDTO.setTitle("New Title");

        mockMvc.perform(patch("/books/{isbn}", "333")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"));
    }

    //Delete
    @WithMockUser
    @Test
    void shouldDeleteBook() throws Exception {
        // insert a book first
        BooksDTO dto = new BooksDTO("David Author", "Delete Me", 40.00, "444");

        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isAccepted());

        // now delete it
        mockMvc.perform(delete("/books/{isbn}", "444"))
                .andExpect(status().isNoContent());

        // confirm it’s gone
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].isbn").value(not(hasItem("444"))));
    }
    
    @WithMockUser
    @Test
    void shouldReturnABook() throws Exception {
        // first insert a book
        BooksDTO dto = new BooksDTO("Author A", "Test Book 2", 20.00, "888");
        
        
        //posting book
        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isAccepted());
        
        MvcResult result = mockMvc.perform(get("/books/888")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
        
        String json = result.getResponse().getContentAsString();
        
        Books book = objectMapper.readValue(
                json, new TypeReference<Books>() {});
        
        assertThat(book.getIsbn()).isEqualTo("888");

    }
}
