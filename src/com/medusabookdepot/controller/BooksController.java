package com.medusabookdepot.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import com.medusabookdepot.controller.files.FileManager;
import com.medusabookdepot.model.modelImpl.StandardBookImpl;
import com.medusabookdepot.model.modelInterface.Depot;
import com.medusabookdepot.model.modelInterface.StandardBook;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class BooksController {

	private final static String NAME = "books"; // Name of the file, for the FileManager class
	private static final int ISBN_LENGTH = 13;
	private final ObservableList<StandardBookImpl> books = FXCollections.observableArrayList();
	private static BooksController singBook;
	
	private FileManager<StandardBookImpl> fileManager = new FileManager<>(books, StandardBookImpl.class, NAME);

	private BooksController() {
		super();
	}

	/**
	 * Load the BooksController object or create a new if it doesn't exists
	 */
	public static BooksController getInstanceOf() {

		return (BooksController.singBook == null ? new BooksController() : BooksController.singBook);
	}

	/**
	 * Convert price from string to integer
	 * 
	 * @param <b>Book
	 *            price</b> in string format
	 * @return Price in integet format
	 * @throws IllegalArgumentException
	 *             and IndexOutOfBoundsException
	 */
	public int convertPrice(String price) throws IllegalArgumentException, IndexOutOfBoundsException {

		if (price.equals(""))
			throw new IllegalArgumentException("The price field mustn't be empty!");

		if (!price.contains(".") && !price.contains(",")) {
			price += ".00";
		} else if (price.charAt(price.length() - 2) == '.' || price.charAt(price.length() - 2) == ',') {
			price += "0";
		} else if (price.charAt(price.length() - 3) == '.' || price.charAt(price.length() - 3) == ',') {
			// Correct input, nothing to do
		} else
			throw new IllegalArgumentException("Price format not valid! (IE 12.50)");

		return Integer.parseInt(new StringBuilder(price).deleteCharAt(price.length() - 3).toString());
	}

	/**
	 * Add a new book in the list
	 * 
	 * @param ISBN
	 * @param Name
	 * @param Year
	 * @param Pages
	 * @param Serie
	 * @param Genre
	 * @param Author
	 * @param <b>Price</b>
	 *            in string format
	 * @throws IllegalArgumentException
	 *             if ISBN already exists
	 */
	public void addBook(String isbn, String name, String year, String pages, String serie, String genre, String author,
			String price) throws IllegalArgumentException, IndexOutOfBoundsException {

		if (this.isInputValid(isbn, year, pages, serie, genre, author)) {

			books.add(new StandardBookImpl(isbn, name, Integer.parseInt(year), Integer.parseInt(pages), serie, genre,
					author, this.convertPrice(price)));
			fileManager.saveDataToFile();
		}
	}

	/**
	 * Search a book in the list
	 * 
	 * @param Book
	 * @return StandardBook if it found the book, else <b>null</b>
	 */
	public StandardBookImpl searchBook(StandardBook book) {

		for (StandardBookImpl b : books) {
			if (book.equals(b)) {

				return b;
			}
		}
		return null;
	}

	/**
	 * Search a string in ALL fields of book object and add it to results if it
	 * is contained in field
	 * 
	 * @param String
	 *            to search
	 * @return <b>List</b> of all books found
	 */
	public List<StandardBookImpl> searchBook(String value) {
		List<StandardBookImpl> result = new ArrayList<>();

		this.books.stream().forEach(e -> {

			if (e.getIsbn().contains(value) || e.getTitle().toLowerCase().contains(value.toLowerCase())
					|| Integer.toString(e.getYear()).contains(value) || Integer.toString(e.getPages()).contains(value)
					|| e.getSerie().toLowerCase().contains(value.toLowerCase())
					|| e.getGenre().toLowerCase().contains(value.toLowerCase())
					|| e.getAuthor().toLowerCase().contains(value.toLowerCase())
					|| Integer.toString(e.getPrice()).contains(value)) {
				result.add(e);
			}
		});
		return result;
	}

	/**
	 * Multifilter for books search. It search in the books list if you don't
	 * pass a depot, or in a specific depot if you pass it
	 * 
	 * @param <b>Depot</b>
	 *            if you want to search in a specific depot
	 * @param ISBN
	 * @param Name
	 * @param Year
	 * @param Pages
	 * @param Serie
	 * @param Genre
	 * @param Author
	 * @return <b>Stream<of StandardBooks></b> found by filtering the books list
	 */
	public Stream<StandardBookImpl> searchBook(Optional<Depot> depot, Optional<String> isbn, Optional<String> name,
			Optional<String> year, Optional<String> pages, Optional<String> serie, Optional<String> genre,
			Optional<String> author) {

		Stream<StandardBookImpl> result = this.books.stream();

		if (isbn.isPresent()) {
			result = result.filter(e -> e.getIsbn().contains(isbn.get()));
		}
		if (name.isPresent()) {
			result = result.filter(e -> e.getTitle().toLowerCase().contains(name.get().toLowerCase()));
		}
		if (year.isPresent()) {
			result = result.filter(e -> Integer.toString(e.getYear()).contains(year.get()));
		}
		if (pages.isPresent()) {
			result = result.filter(e -> Integer.toString(e.getPages()).contains(pages.get()));
		}
		if (serie.isPresent()) {
			result = result.filter(e -> e.getSerie().toLowerCase().contains(serie.get().toLowerCase()));
		}
		if (genre.isPresent()) {
			result = result.filter(e -> e.getGenre().toLowerCase().contains(genre.get().toLowerCase()));
		}
		if (author.isPresent()) {
			result = result.filter(e -> e.getAuthor().toLowerCase().contains(author.get().toLowerCase()));
		}
		if (depot.isPresent()) {

			result = result.filter(e -> depot.filter(f -> f.getQuantityFromStandardBook(e) < 1) != null);
		}

		return result;
	}

	/**
	 * Remove a book from the list
	 * 
	 * @param Book
	 * @throws NoSuchElementException
	 *             if element is not present in books list
	 */
	public void removeBook(StandardBook book) throws NoSuchElementException {

		try {
			books.remove(book);
		} catch (Exception e) {
			throw new NoSuchElementException("No such element in list!");
		}
		fileManager.saveDataToFile();
	}

	/**
	 * Search isbn and remove relative book
	 * 
	 * @param isbn
	 *            of book to remove
	 */
	public void removeBook(String isbn) {

		this.searchBook(Optional.empty(), Optional.of(isbn), Optional.empty(), Optional.empty(), Optional.empty(),
				Optional.empty(), Optional.empty(), Optional.empty()).forEach(e -> {
					this.removeBook(e);
				});
	}

	/**
	 * Check if the input for a new book is correct
	 * 
	 * @param ISBN
	 * @param Year
	 * @param Serie
	 * @param Genre
	 * @param Author
	 * @return <b>True</b> if input is valid, else a exception
	 * @throws IllegalArgumentException
	 *             if the arguments are not valid
	 */
	public boolean isInputValid(String isbn, String year, String pages, String serie, String genre, String author)
			throws IllegalArgumentException {

		if (isbn.equals("") || year.equals("") || pages.equals("") || serie.equals("") || genre.equals("")
				|| author.equals("")) {

			throw new IllegalArgumentException("The fields mustn't be empty!");
		}
		
		try {
			Integer.parseInt(year);
			Integer.parseInt(pages);
		} catch (Exception e) {
			throw new IllegalArgumentException("Year and pages must be integers!");
		}
		if (this.searchBook(Optional.empty(), Optional.of(isbn), Optional.empty(), Optional.empty(), Optional.empty(),
				Optional.empty(), Optional.empty(), Optional.empty()).count() >= 1) {
			throw new IllegalArgumentException(isbn + " is already present!");
		}
		if (isbn.length() != ISBN_LENGTH) {
			throw new IllegalArgumentException(isbn + " should be " + ISBN_LENGTH + " character!");
		}
		if (year.length() != 4
				|| Integer.parseInt(year) > java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)) {
			throw new IllegalArgumentException(
					"Wait a minute, Doc. Are you telling me that you built a time machine... out of Delorian?!");
		}

		return true;
	}

	/**
	 * Convert the XML file to PDF
	 * 
	 * @throws IOException
	 */
	public void convert() throws IOException {

		fileManager.convertXML2PDF();

	}

	/**
	 * Return a ObservableList with all ISBNs relative a title (A title may  to more than one ISBN, like "Introduction in Java") 
	 * @param Title
	 */
	public ObservableList<String> getAllIsbnFromTitle(String title){
		
		ObservableList<String> titles = FXCollections.observableArrayList();
		this.searchBook(Optional.empty(), Optional.empty(), Optional.of(title), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()).forEach(e->{
			titles.add(e.getIsbn());
		});
		return titles;
	}
	/**
	 * Edit isbn number
	 * 
	 * @param Book
	 * @param isbn
	 * @throws IllegalArgumentException
	 */
	public void editISBN(StandardBook book, String isbn) {

		if (!this.isInputValid(isbn, Integer.toString(book.getYear()), Integer.toString(book.getPages()),
				book.getSerie(), book.getGenre(), book.getAuthor())) {
			throw new IllegalArgumentException("ISBN is not valid!");
		}
		books.get(books.indexOf(book)).setIsbn(isbn);
		fileManager.saveDataToFile();
	}

	/**
	 * Edit book name
	 * 
	 * @param Book
	 * @param Name
	 * @throws IllegalArgumentException
	 *             if the argument passed is empty
	 */
	public void editTitle(StandardBook book, String title) {

		if (title.equals("")) {
			throw new IllegalArgumentException("The argument must be not empty!");
		}
		books.get(books.indexOf(book)).setTitle(title);
		fileManager.saveDataToFile();
	}

	/**
	 * Edit book release year
	 * 
	 * @param Book
	 * @param Year
	 * @throws IllegalArgumentException
	 */
	public void editYear(StandardBook book, String year) {

		if (!this.isInputValid(book.getIsbn(), year, Integer.toString(book.getPages()), book.getSerie(),
				book.getGenre(), book.getAuthor())) {
			throw new IllegalArgumentException("Year is not valid!");
		}
		books.get(books.indexOf(book)).setYear(Integer.parseInt(year));
		fileManager.saveDataToFile();
	}

	/**
	 * Edit book pages number
	 * 
	 * @param Book
	 * @param Pages
	 * @throws IllegalArgumentException
	 */
	public void editPages(StandardBook book, String pages) {

		if (!this.isInputValid(book.getIsbn(), Integer.toString(book.getYear()), pages, book.getSerie(),
				book.getGenre(), book.getAuthor())) {
			throw new IllegalArgumentException("Pages number is not valid!");
		}
		books.get(books.indexOf(book)).setPages(Integer.parseInt(pages));
		fileManager.saveDataToFile();
	}

	/**
	 * Edit book serie
	 * 
	 * @param Book
	 * @param Serie
	 * @throws IllegalArgumentException
	 *             if the argument passed is empty
	 */
	public void editSerie(StandardBook book, String serie) {

		if (serie.equals("")) {
			throw new IllegalArgumentException("The argument must be not empty!");
		}
		books.get(books.indexOf(book)).setSerie(serie);
		fileManager.saveDataToFile();
	}

	/**
	 * Edit book genre
	 * 
	 * @param Book
	 * @param Genre
	 * @throws IllegalArgumentException
	 *             if the argument passed is empty
	 */
	public void editGenre(StandardBook book, String genre) {

		if (genre.equals("")) {
			throw new IllegalArgumentException("The argument must be not empty!");
		}
		books.get(books.indexOf(book)).setGenre(genre);
		fileManager.saveDataToFile();
	}

	/**
	 * Edit book author
	 * 
	 * @param Book
	 * @param Author
	 * @throws IllegalArgumentException
	 *             if the argument passed is empty
	 */
	public void editAuthor(StandardBook book, String author) {

		if (author.equals("")) {
			throw new IllegalArgumentException("The argument must be not empty!");
		}
		books.get(books.indexOf(book)).setAuthor(author);
		fileManager.saveDataToFile();
	}

	/**
	 * Edit book price
	 * 
	 * @param Book
	 * @param Price
	 */
	public void editPrice(StandardBook book, String price) {

		books.get(books.indexOf(book)).setPrice(this.convertPrice(price));
		fileManager.saveDataToFile();
	}

	/**
	 * @return The list of saved books
	 */
	public ObservableList<StandardBookImpl> getBooks() {

		return books;
	}
}
