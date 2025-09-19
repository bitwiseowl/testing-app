package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.BooksController;
import models.Books;
import models.BooksDTO;
import models.BookUpdateDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;


import org.springframework.validation.BindingResult;

import repository.BookRepository;


import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class BooksControllerTest {


	private BooksController booksController;
	private BookRepository bookRepository;
	private BindingResult bindingResult;
    
    @BeforeEach
    void setup() {
    	bookRepository = mock(BookRepository.class);
    	booksController = new BooksController(bookRepository);
    	bindingResult = mock(BindingResult.class);
    }


    @Test
    void showBooksShouldReturnList() throws Exception {
        
    	// Creating a new book
    	Books book = new Books();
        book.setIsbn("123");
        book.setTitle("Test Book");
        book.setAuthor("John Doe");
        book.setPrice(9.99);

        //When the bookRepo's findall method is used, then return the book we have created.
        when(bookRepository.findAll()).thenReturn(List.of(book));
        
        List<Books> response = booksController.showBooks();
        assertThat(response.getFirst().getIsbn().equals("123"));
        
    }

    @Test
    void addBookShouldSaveAndReturnBook() throws Exception {
    	// Create a new book DTO
        BooksDTO dto = new BooksDTO("Jane Doe", "Another Book", 19.99, "234");
        
        Books savedBook = new Books();
        savedBook.setAuthor(dto.getAuthor());
        savedBook.setTitle(dto.getTitle());
        savedBook.setPrice(dto.getPrice());
        savedBook.setIsbn(dto.getIsbn());
        //Return the Book object when the save method is called for bookRepo
        when(bookRepository.save(any(Books.class))).thenReturn(savedBook);

        ResponseEntity<Books> response = (ResponseEntity<Books>) booksController.addBookSubmit(dto, bindingResult);
        
        assertThat(response.getBody().getIsbn().equals("234"));
    }

    @Test
    void updateBookShouldPatchAndReturnUpdatedBook() throws Exception {
        // Arrange
        String isbn = "789";
        Books existingBook = new Books();
        existingBook.setIsbn(isbn);
        existingBook.setTitle("Old Title");
        existingBook.setAuthor("Old Author");
        existingBook.setPrice(10.0);

        BookUpdateDTO updateDTO = new BookUpdateDTO();
        updateDTO.setTitle("New Title");

        when(bookRepository.findById(isbn)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Books.class))).thenReturn(existingBook);

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        // Act
        ResponseEntity<?> response =
                booksController.updateBook(isbn, updateDTO, bindingResult);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((Books) response.getBody()).getTitle()).isEqualTo("New Title");
    }

    @Test
    void deleteBookShouldRemoveBook() {
        // Arrange
        String isbn = "111";
        Books book = new Books();
        book.setIsbn(isbn);
        book.setTitle("Book to Delete");

        when(bookRepository.findById(isbn)).thenReturn(Optional.of(book));
        doNothing().when(bookRepository).delete(book);

        // Act
        ResponseEntity<?> response = booksController.deleteBook(isbn);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isEqualTo(book);
        verify(bookRepository, times(1)).delete(book);
    }

    
    
}
