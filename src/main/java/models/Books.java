package models;


import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="books")
public class Books {

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Books)) return false;
        Books book = (Books) o;
        return Objects.equals(isbn, book.isbn) &&
               Objects.equals(title, book.title) &&
               Objects.equals(author, book.author) &&
               Objects.equals(price, book.price);
    }
	
	@Id
	@Column(nullable=false, unique=true)
	private String isbn;
	
	@Column(nullable = false)
	private String author;

	@Column(nullable = false)
	private String title;
	
	private double price;
	
	

	
}
