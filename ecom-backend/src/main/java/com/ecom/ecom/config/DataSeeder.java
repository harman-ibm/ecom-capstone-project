package com.ecom.ecom.config;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.ecom.ecom.model.Book;
import com.ecom.ecom.repository.BookRepository;

/** Seeds 50 demo books (with cover and author images) the first time the app starts with an empty database. */
@Component
public class DataSeeder implements ApplicationRunner {

	private static final Map<String, String> AUTHOR_BIOS = Map.ofEntries(
			Map.entry("Alex Michaelides", "Alex Michaelides is a British-Cypriot author and screenwriter. His debut novel, The Silent Patient, became an international bestseller and has been translated into more than 40 languages."),
			Map.entry("Gillian Flynn", "Gillian Flynn is an American author and former journalist known for dark, twisting psychological thrillers. Gone Girl was adapted into an acclaimed feature film."),
			Map.entry("Frank Herbert", "Frank Herbert (1920-1986) was an American science fiction writer and journalist. Dune, first published in 1965, is widely regarded as one of the greatest science fiction novels ever written."),
			Map.entry("William Gibson", "William Gibson is an American-Canadian writer who helped found the cyberpunk genre. He coined the term \"cyberspace\" in his short fiction before Neuromancer made it famous."),
			Map.entry("Jane Austen", "Jane Austen (1775-1817) was an English novelist whose sharp social commentary and witty romances, including Pride and Prejudice and Emma, remain among the most loved in English literature."),
			Map.entry("Diana Gabaldon", "Diana Gabaldon is an American author and former university professor of scientific research. Her Outlander series blends historical fiction, romance and adventure."),
			Map.entry("J.R.R. Tolkien", "J.R.R. Tolkien (1892-1973) was an English author and Oxford professor of languages. He created Middle-earth in The Hobbit and The Lord of the Rings, shaping modern fantasy."),
			Map.entry("George R.R. Martin", "George R.R. Martin is an American novelist and screenwriter. His epic fantasy series A Song of Ice and Fire inspired the television series Game of Thrones."),
			Map.entry("Yuval Noah Harari", "Yuval Noah Harari is an Israeli historian and professor at the Hebrew University of Jerusalem. His books explore the past and future of humankind for a general audience."),
			Map.entry("Tara Westover", "Tara Westover is an American author and historian. Raised in rural Idaho without formal schooling, she went on to earn a PhD from the University of Cambridge."),
			Map.entry("James Clear", "James Clear is an American author and speaker who writes about habits, decision making and continuous improvement. Atomic Habits has sold millions of copies worldwide."),
			Map.entry("Robert C. Martin", "Robert C. Martin, known as \"Uncle Bob\", is an American software engineer and author. He is a leading voice in clean code, agile development and software craftsmanship."),
			Map.entry("Stieg Larsson", "Stieg Larsson (1954-2004) was a Swedish journalist and novelist. His Millennium trilogy, beginning with The Girl with the Dragon Tattoo, was published after his death and became a worldwide phenomenon."),
			Map.entry("Dan Brown", "Dan Brown is an American author of thriller novels featuring Harvard symbologist Robert Langdon. The Da Vinci Code is one of the best-selling novels of all time."),
			Map.entry("Liane Moriarty", "Liane Moriarty is an Australian author of contemporary fiction. Her novels mix domestic drama and mystery, and Big Little Lies was adapted into an award-winning television series."),
			Map.entry("Agatha Christie", "Agatha Christie (1890-1976) was an English crime writer and creator of detectives Hercule Poirot and Miss Marple. She is the best-selling novelist of all time."),
			Map.entry("Paula Hawkins", "Paula Hawkins is a British author and former journalist. Her psychological thriller The Girl on the Train sold more than 20 million copies worldwide."),
			Map.entry("Isaac Asimov", "Isaac Asimov (1920-1992) was a Russian-born American writer and professor of biochemistry. He wrote or edited more than 500 books, including the Foundation and Robot series."),
			Map.entry("Ursula K. Le Guin", "Ursula K. Le Guin (1929-2018) was an American author of science fiction and fantasy. She is celebrated for the Earthsea cycle and for novels that explore gender, society and politics."),
			Map.entry("Orson Scott Card", "Orson Scott Card is an American author best known for Ender's Game, which won both the Hugo and Nebula awards and launched a long-running series."),
			Map.entry("Andy Weir", "Andy Weir is an American author and former software engineer. He first self-published The Martian online, and it was later adapted into a Hollywood film."),
			Map.entry("George Orwell", "George Orwell (1903-1950) was an English novelist and essayist. His dystopian novel Nineteen Eighty-Four and the satire Animal Farm are classics of political fiction."),
			Map.entry("Jojo Moyes", "Jojo Moyes is an English novelist and former journalist. Her romantic fiction, including Me Before You, has sold tens of millions of copies."),
			Map.entry("Nicholas Sparks", "Nicholas Sparks is an American novelist known for emotional love stories. Many of his books, including The Notebook, have been adapted into films."),
			Map.entry("Charlotte Bront\u00eb", "Charlotte Bront\u00eb (1816-1855) was an English novelist and the eldest of the Bront\u00eb sisters. Jane Eyre, published in 1847 under the pen name Currer Bell, is her best-known work."),
			Map.entry("J.K. Rowling", "J.K. Rowling is a British author best known for the Harry Potter fantasy series, which has sold more than 500 million copies and been adapted into a global film franchise."),
			Map.entry("Patrick Rothfuss", "Patrick Rothfuss is an American author of epic fantasy. The Name of the Wind is the first book in his Kingkiller Chronicle series."),
			Map.entry("C.S. Lewis", "C.S. Lewis (1898-1963) was a British writer, scholar and friend of J.R.R. Tolkien. He wrote The Chronicles of Narnia, one of the best-loved series in children's fantasy."),
			Map.entry("Neil Gaiman", "Neil Gaiman is an English author of novels, comics and screenplays. His work includes The Sandman, Coraline and American Gods."),
			Map.entry("Jared Diamond", "Jared Diamond is an American geographer and professor at UCLA. He won the Pulitzer Prize for Guns, Germs, and Steel, which examines why human societies developed differently."),
			Map.entry("Anne Frank", "Anne Frank (1929-1945) was a German-born Jewish girl who documented two years in hiding in Amsterdam during the Second World War. Her diary was published after the war."),
			Map.entry("Howard Zinn", "Howard Zinn (1922-2010) was an American historian, playwright and activist. His best-known work, A People's History of the United States, tells American history from the viewpoint of ordinary people."),
			Map.entry("Michelle Obama", "Michelle Obama is an American lawyer, author and advocate who served as First Lady of the United States from 2009 to 2017. Becoming is one of the best-selling memoirs ever published."),
			Map.entry("Trevor Noah", "Trevor Noah is a South African comedian, author and television host. Born a Crime recounts his childhood growing up in South Africa under apartheid."),
			Map.entry("Maya Angelou", "Maya Angelou (1928-2014) was an American poet, memoirist and civil rights activist. I Know Why the Caged Bird Sings was the first of her seven autobiographies."),
			Map.entry("Jeannette Walls", "Jeannette Walls is an American journalist and author. Her memoir The Glass Castle spent several years on the New York Times best-seller list."),
			Map.entry("Stephen R. Covey", "Stephen R. Covey (1932-2012) was an American educator, businessman and author. The 7 Habits of Highly Effective People is one of the best-selling non-fiction books of the twentieth century."),
			Map.entry("Dale Carnegie", "Dale Carnegie (1888-1955) was an American writer and lecturer who developed courses on self-improvement, public speaking and interpersonal skills."),
			Map.entry("Cal Newport", "Cal Newport is an American computer science professor at Georgetown University and author who writes about productivity, focus and technology in modern life."),
			Map.entry("Daniel Kahneman", "Daniel Kahneman (1934-2024) was an Israeli-American psychologist who won the 2002 Nobel Memorial Prize in Economic Sciences for his work on judgment and decision making."),
			Map.entry("Andrew Hunt", "Andrew Hunt is an American software developer and author. With David Thomas he wrote The Pragmatic Programmer and co-founded the Pragmatic Bookshelf publishing company."),
			Map.entry("Martin Fowler", "Martin Fowler is a British software developer, author and speaker. He is known for his writing on refactoring, software architecture and agile methods."),
			Map.entry("Martin Kleppmann", "Martin Kleppmann is a British researcher and software engineer who works on distributed systems and data infrastructure. He wrote Designing Data-Intensive Applications."));

	private final BookRepository books;

	public DataSeeder(BookRepository books) {
		this.books = books;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (books.count() > 0) {
			return;
		}
		books.saveAll(List.of(
				// Thriller
				book("The Silent Patient", "Alex Michaelides", "Thriller", 1499, 336, 4.5, "Alicia Berenson shoots her husband five times and then never speaks another word. A psychotherapist becomes obsessed with uncovering why, in a twisting psychological thriller."),
				book("Gone Girl", "Gillian Flynn", "Thriller", 1399, 432, 4, "On their fifth anniversary, Amy disappears. As the police close in on her husband Nick, nothing about their marriage turns out to be what it seemed."),
				book("Dune", "Frank Herbert", "Science Fiction", 1799, 688, 5, "Paul Atreides must survive on the desert planet Arrakis, the only source of the universe's most valuable substance, in this landmark of science fiction."),
				book("Neuromancer", "William Gibson", "Science Fiction", 1299, 320, 4, "Case, a washed-up hacker, is hired for one last job in cyberspace. The novel that defined the cyberpunk genre."),
				book("Pride and Prejudice", "Jane Austen", "Romance", 899, 279, 5, "Elizabeth Bennet and the proud Mr. Darcy navigate manners, money and misjudgement in Austen's witty classic."),
				book("Outlander", "Diana Gabaldon", "Romance", 1599, 850, 4, "A nurse in 1945 is swept back to 1743 Scotland, where she is caught between two men and two lives."),
				book("The Hobbit", "J.R.R. Tolkien", "Fantasy", 1199, 310, 5, "Bilbo Baggins is swept into an epic quest to reclaim a dwarf kingdom from the dragon Smaug."),
				book("A Game of Thrones", "George R.R. Martin", "Fantasy", 1699, 694, 4.5, "Noble families fight for the Iron Throne of Westeros while an ancient threat gathers in the north."),
				book("Sapiens", "Yuval Noah Harari", "History", 1899, 464, 4.5, "A sweeping history of humankind, from the first humans to the age of artificial intelligence."),
				book("Educated", "Tara Westover", "Memoir", 1499, 352, 4.5, "A woman raised by survivalists in rural Idaho teaches herself enough to earn a PhD from Cambridge."),
				book("Atomic Habits", "James Clear", "Self-Help", 1699, 320, 5, "A practical framework for building good habits and breaking bad ones through tiny, consistent changes."),
				book("Clean Code", "Robert C. Martin", "Technology", 3999, 464, 4.5, "A handbook of agile software craftsmanship: how to write code that is readable, maintainable and professional."),

				book("The Girl with the Dragon Tattoo", "Stieg Larsson", "Thriller", 1499, 465, 4.0, "A disgraced journalist and a brilliant, troubled hacker investigate a wealthy family's forty-year-old disappearance."),
				book("The Da Vinci Code", "Dan Brown", "Thriller", 1399, 454, 3.5, "A murder in the Louvre sends symbologist Robert Langdon on a race through art, history and secret societies."),
				book("Big Little Lies", "Liane Moriarty", "Thriller", 1299, 460, 4.0, "Three mothers' lives unravel at a school trivia night in a sharp, funny story of secrets and suburban rivalry."),
				book("And Then There Were None", "Agatha Christie", "Thriller", 999, 272, 4.5, "Ten strangers are lured to an island and begin to die one by one, following an old nursery rhyme."),
				book("Murder on the Orient Express", "Agatha Christie", "Thriller", 999, 256, 4.0, "Detective Hercule Poirot must find a killer among the passengers of a train stranded in the snow."),
				book("The Girl on the Train", "Paula Hawkins", "Thriller", 1199, 395, 3.5, "A commuter becomes entangled in a missing-person case after watching a couple from her daily train."),

				book("Foundation", "Isaac Asimov", "Science Fiction", 1199, 255, 4.5, "Mathematician Hari Seldon predicts the fall of the Galactic Empire and sets a plan in motion to shorten the dark age that follows."),
				book("I, Robot", "Isaac Asimov", "Science Fiction", 1099, 224, 4.0, "Linked stories about robots, the Three Laws of Robotics and the people who live and work beside them."),
				book("The Left Hand of Darkness", "Ursula K. Le Guin", "Science Fiction", 1399, 304, 4.5, "An envoy to the icy world of Gethen confronts a society with no fixed gender in a landmark of the genre."),
				book("Ender's Game", "Orson Scott Card", "Science Fiction", 1299, 324, 4.5, "Gifted child Ender Wiggin is trained at Battle School to lead humanity against an alien invasion."),
				book("The Martian", "Andy Weir", "Science Fiction", 1499, 384, 4.5, "Stranded alone on Mars, astronaut Mark Watney must use science and humour to survive until rescue."),
				book("Nineteen Eighty-Four", "George Orwell", "Science Fiction", 999, 328, 5, "Winston Smith rebels against a totalitarian state that watches everyone and rewrites the truth."),

				book("Emma", "Jane Austen", "Romance", 899, 474, 4.0, "Clever, wealthy Emma Woodhouse fancies herself a matchmaker, until her schemes begin to backfire."),
				book("Sense and Sensibility", "Jane Austen", "Romance", 899, 352, 4.0, "The Dashwood sisters face heartbreak and money troubles, balancing reason against emotion."),
				book("Me Before You", "Jojo Moyes", "Romance", 1299, 369, 4.5, "A young carer and the man she looks after change each other's lives in a moving, funny love story."),
				book("The Notebook", "Nicholas Sparks", "Romance", 1199, 214, 4.0, "An elderly man reads from a worn notebook to the woman he has loved for a lifetime."),
				book("Jane Eyre", "Charlotte Bront\u00eb", "Romance", 899, 532, 4.5, "An orphaned governess finds love and a dark secret at Thornfield Hall in this Gothic classic."),

				book("The Fellowship of the Ring", "J.R.R. Tolkien", "Fantasy", 1499, 423, 5, "Frodo Baggins leaves the Shire with eight companions to destroy the One Ring."),
				book("A Clash of Kings", "George R.R. Martin", "Fantasy", 1699, 768, 4.5, "Rival kings fight for the throne of Westeros as a comet blazes across the sky."),
				book("Harry Potter and the Philosopher's Stone", "J.K. Rowling", "Fantasy", 1399, 223, 5, "An orphan discovers he is a wizard and begins his first year at Hogwarts School of Witchcraft and Wizardry."),
				book("The Name of the Wind", "Patrick Rothfuss", "Fantasy", 1599, 662, 4.5, "Innkeeper Kvothe tells the story of his life: a gifted boy who became a legend."),
				book("The Lion, the Witch and the Wardrobe", "C.S. Lewis", "Fantasy", 999, 208, 4.5, "Four siblings step through a wardrobe into Narnia, a land trapped in endless winter."),
				book("American Gods", "Neil Gaiman", "Fantasy", 1499, 465, 4.0, "Released from prison, Shadow Moon becomes bodyguard to a mysterious con man in a war between old and new gods."),

				book("Guns, Germs, and Steel", "Jared Diamond", "History", 1799, 528, 4.0, "Why did some civilisations come to dominate others? A sweeping look at geography, biology and the course of history."),
				book("Homo Deus", "Yuval Noah Harari", "History", 1799, 464, 4.0, "What happens when humans gain the power to engineer life and intelligence? A look at the future of our species."),
				book("The Diary of a Young Girl", "Anne Frank", "History", 999, 283, 5, "The diary of a Jewish teenager who spent two years hiding from the Nazis in an Amsterdam annex."),
				book("A People's History of the United States", "Howard Zinn", "History", 1999, 729, 4.0, "American history told from the perspective of workers, women, immigrants and Indigenous peoples."),

				book("Becoming", "Michelle Obama", "Memoir", 1799, 448, 4.5, "The former First Lady reflects on her childhood in Chicago, her career and her years in the White House."),
				book("Born a Crime", "Trevor Noah", "Memoir", 1499, 304, 5, "Stories from a South African childhood under apartheid, told with warmth and humour."),
				book("I Know Why the Caged Bird Sings", "Maya Angelou", "Memoir", 1199, 289, 4.5, "Maya Angelou's account of her early years, and of how literature and resilience carried her through."),
				book("The Glass Castle", "Jeannette Walls", "Memoir", 1399, 288, 4.5, "A memoir of growing up with brilliant, nomadic parents who were unable to provide a stable home."),

				book("The 7 Habits of Highly Effective People", "Stephen R. Covey", "Self-Help", 1699, 381, 4.5, "Seven principles for personal and professional effectiveness, built on character rather than quick fixes."),
				book("How to Win Friends and Influence People", "Dale Carnegie", "Self-Help", 1299, 291, 4.5, "Timeless advice on dealing with people, making friends and handling conflict."),
				book("Deep Work", "Cal Newport", "Self-Help", 1599, 296, 4.0, "Rules for focused success in a distracted world, and for producing work that truly matters."),
				book("Thinking, Fast and Slow", "Daniel Kahneman", "Self-Help", 1799, 499, 4.5, "A Nobel laureate explains the two systems that drive how we think, and where they go wrong."),

				book("The Pragmatic Programmer", "Andrew Hunt", "Technology", 4499, 352, 4.5, "Practical, career-spanning advice for becoming a more effective and adaptable software developer."),
				book("Refactoring", "Martin Fowler", "Technology", 4799, 448, 4.5, "A catalogue of techniques for improving the design of existing code without changing its behaviour."),
				book("Designing Data-Intensive Applications", "Martin Kleppmann", "Technology", 4999, 616, 5, "The big ideas behind reliable, scalable and maintainable data systems.")));
	}

	/** Cover and author images are deterministic per title/author so the same record always shows the same picture. */
	private static Book book(String title, String author, String genre, int priceCents, int pages, double rating,
			String description) {
		String slug = title.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
		String cover = "https://picsum.photos/seed/" + slug + "/400/600";
		String authorImage = "https://api.dicebear.com/9.x/initials/svg?seed="
				+ URLEncoder.encode(author, StandardCharsets.UTF_8);
		return new Book(title, author, genre, priceCents, pages, rating, description, AUTHOR_BIOS.get(author), cover,
				authorImage);
	}
}
