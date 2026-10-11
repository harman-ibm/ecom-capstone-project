package com.ecom.ecom;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTests {

	private static final String V1 = "/api/v1";

	@Autowired
	MockMvc mvc;

	private final ObjectMapper json = new ObjectMapper();

	// ---------- helpers ----------

	private String signup(String name, String email, String password) throws Exception {
		MvcResult r = mvc.perform(post(V1 + "/auth/signup").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"confirmPassword\":\"%s\"}".formatted(name, email, password, password)))
				.andExpect(status().isCreated()).andReturn();
		return json.readTree(r.getResponse().getContentAsString()).get("token").asText();
	}

	private static String orderBody(long bookId, int qty, String cardNumber, String expiry) {
		return """
				{"items":[{"bookId":%d,"quantity":%d}],
				 "shipping":{"fullName":"Ann Lee","address":"1 Main St","city":"Town","state":"TX","postalCode":"75001","country":"USA","phone":"5551234","method":"standard"},
				 "payment":{"cardName":"Ann Lee","cardNumber":"%s","expiry":"%s","cvc":"123"}}
				""".formatted(bookId, qty, cardNumber, expiry);
	}

	private static String futureExpiry() {
		return YearMonth.now().plusYears(2).format(DateTimeFormatter.ofPattern("MM/yy"));
	}

	private long firstBookId() throws Exception {
		MvcResult r = mvc.perform(get(V1 + "/books")).andExpect(status().isOk()).andReturn();
		return json.readTree(r.getResponse().getContentAsString()).get(0).get("id").asLong();
	}

	// ---------- public catalogue ----------

	@Test
	void catalogueIsPublicAndSearchable() throws Exception {
		mvc.perform(get(V1 + "/books")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(50)))
				.andExpect(jsonPath("$[0].imageUrl").isNotEmpty())
				.andExpect(jsonPath("$[0].authorImageUrl").isNotEmpty());
		mvc.perform(get(V1 + "/books").param("by", "genre").param("q", "romance"))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(7)))
				.andExpect(jsonPath("$[0].genre").value("Romance"));
		mvc.perform(get(V1 + "/books").param("by", "author").param("q", "tolkien"))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
		mvc.perform(get(V1 + "/books").param("by", "nonsense")).andExpect(status().isBadRequest());
	}

	@Test
	void bookDetailIncludesAuthorBioAndMissingBookIs404() throws Exception {
		long id = firstBookId();
		mvc.perform(get(V1 + "/books/" + id)).andExpect(status().isOk())
				.andExpect(jsonPath("$.authorBio").isNotEmpty())
				.andExpect(jsonPath("$.priceCents").isNumber());
		mvc.perform(get(V1 + "/books/99999")).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Book not found"));
	}

	@Test
	void recommendationsRankSameGenreFirstAndExcludeSelf() throws Exception {
		MvcResult dune = mvc.perform(get(V1 + "/books").param("q", "dune")).andReturn();
		long duneId = json.readTree(dune.getResponse().getContentAsString()).get(0).get("id").asLong();
		mvc.perform(get(V1 + "/books/" + duneId + "/recommendations"))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(5)))
				.andExpect(jsonPath("$[0].genre").value("Science Fiction"))
				.andExpect(jsonPath("$[?(@.title=='Dune')]").isEmpty());
	}

	// ---------- auth + validation (all server side) ----------

	@Test
	void signupReturnsJwtAndNeverExposesThePassword() throws Exception {
		mvc.perform(post(V1 + "/auth/signup").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Ann\",\"email\":\"Ann.Case@Example.com\",\"password\":\"secret1\",\"confirmPassword\":\"secret1\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.user.email").value("ann.case@example.com"))
				.andExpect(jsonPath("$.user.password").doesNotExist())
				.andExpect(jsonPath("$.user.passwordHash").doesNotExist());
	}
	@Test
	void passwordMismatchIsRejectedByTheServer() throws Exception {
		mvc.perform(post(V1 + "/auth/signup").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Mis\",\"email\":\"mis@example.com\",\"password\":\"secret1\",\"confirmPassword\":\"secret2\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Passwords do not match"));
	}

	@Test
	void shippingCanBePreValidatedWithoutStoringAnything() throws Exception {
		String token = signup("Ship", "ship@example.com", "secret1");
		mvc.perform(post(V1 + "/orders/validate-shipping").header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"fullName\":\"Ann\",\"address\":\"1 Main\",\"city\":\"T\",\"state\":\"TX\",\"postalCode\":\"1\",\"country\":\"USA\",\"phone\":\"55555\",\"method\":\"express\"}"))
				.andExpect(status().isNoContent());
		mvc.perform(post(V1 + "/orders/validate-shipping").header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"fullName\":\"\",\"address\":\"1 Main\",\"city\":\"T\",\"state\":\"TX\",\"postalCode\":\"1\",\"country\":\"USA\",\"phone\":\"55555\",\"method\":\"express\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors[0].field").value("fullName"));
		mvc.perform(get(V1 + "/orders").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
		mvc.perform(post(V1 + "/orders/validate-shipping").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isUnauthorized());
	}



	@Test
	void signupValidationReturnsFieldErrors() throws Exception {
		mvc.perform(post(V1 + "/auth/signup").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"\",\"email\":\"not-an-email\",\"password\":\"123\",\"confirmPassword\":\"123\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.fieldErrors[?(@.field=='name')]").isNotEmpty())
				.andExpect(jsonPath("$.fieldErrors[?(@.field=='email')]").isNotEmpty())
				.andExpect(jsonPath("$.fieldErrors[?(@.field=='password')].message")
						.value("Password must be 6 to 100 characters"));
	}

	@Test
	void duplicateEmailIs409() throws Exception {
		signup("Dup", "dup@example.com", "secret1");
		mvc.perform(post(V1 + "/auth/signup").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Dup\",\"email\":\"DUP@example.com\",\"password\":\"secret1\",\"confirmPassword\":\"secret1\"}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("An account with this email already exists"));
	}

	@Test
	void loginSucceedsAndWrongCredentialsGiveTheSame401() throws Exception {
		signup("Lou", "lou@example.com", "secret1");
		mvc.perform(post(V1 + "/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"lou@example.com\",\"password\":\"secret1\"}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty());
		mvc.perform(post(V1 + "/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"lou@example.com\",\"password\":\"wrong-pass\"}"))
				.andExpect(status().isUnauthorized()).andExpect(jsonPath("$.message").value("Invalid email or password"));
		mvc.perform(post(V1 + "/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"nobody@example.com\",\"password\":\"secret1\"}"))
				.andExpect(status().isUnauthorized()).andExpect(jsonPath("$.message").value("Invalid email or password"));
	}

	@Test
	void malformedJsonIs400() throws Exception {
		mvc.perform(post(V1 + "/auth/login").contentType(MediaType.APPLICATION_JSON).content("{not json"))
				.andExpect(status().isBadRequest());
	}

	// ---------- JWT protection of /orders ----------

	@Test
	void ordersRequireAuthentication() throws Exception {
		mvc.perform(get(V1 + "/orders")).andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401));
		mvc.perform(post(V1 + "/orders").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isUnauthorized());
		mvc.perform(get(V1 + "/orders").header("Authorization", "Bearer garbage.token.value"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void unversionedPathsDoNotExist() throws Exception {
		mvc.perform(get("/api/books")).andExpect(status().isUnauthorized());
	}

	// ---------- orders ----------

	@Test
	void placingAnOrderPricesItOnTheServerAndAppearsInHistory() throws Exception {
		String token = signup("Ann", "order.ann@example.com", "secret1");
		long bookId = firstBookId();

		MvcResult placed = mvc.perform(post(V1 + "/orders").header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(orderBody(bookId, 2, "4242 4242 4242 4242", futureExpiry())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("Processing"))
				.andExpect(jsonPath("$.items", hasSize(1)))
				.andExpect(jsonPath("$.items[0].quantity").value(2))
				.andReturn();

		JsonNode order = json.readTree(placed.getResponse().getContentAsString());
		int unit = order.get("items").get(0).get("unitPriceCents").asInt();
		org.junit.jupiter.api.Assertions.assertEquals(unit * 2, order.get("totalCents").asInt());
		// Card data must never come back or be stored.
		org.junit.jupiter.api.Assertions.assertFalse(placed.getResponse().getContentAsString().contains("4242"));

		mvc.perform(get(V1 + "/orders").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(order.get("id").asLong()))
				.andExpect(jsonPath("$[0].shipping.fullName").value("Ann Lee"))
				.andExpect(jsonPath("$[0].items[0].book.title").isNotEmpty());
	}

	@Test
	void usersCannotSeeEachOthersOrders() throws Exception {
		String ann = signup("Ann", "iso.ann@example.com", "secret1");
		String bob = signup("Bob", "iso.bob@example.com", "secret1");
		MvcResult placed = mvc.perform(post(V1 + "/orders").header("Authorization", "Bearer " + ann)
				.contentType(MediaType.APPLICATION_JSON)
				.content(orderBody(firstBookId(), 1, "4242424242424242", futureExpiry())))
				.andExpect(status().isCreated()).andReturn();
		long orderId = json.readTree(placed.getResponse().getContentAsString()).get("id").asLong();

		mvc.perform(get(V1 + "/orders").header("Authorization", "Bearer " + bob))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
		mvc.perform(get(V1 + "/orders/" + orderId).header("Authorization", "Bearer " + bob))
				.andExpect(status().isNotFound());
		mvc.perform(get(V1 + "/orders/" + orderId).header("Authorization", "Bearer " + ann))
				.andExpect(status().isOk());
	}

	@Test
	void orderValidationErrorsAreReportedPerField() throws Exception {
		String token = signup("Val", "val@example.com", "secret1");
		String bad = """
				{"items":[{"bookId":1,"quantity":0}],
				 "shipping":{"fullName":"","address":"1 Main St","city":"Town","state":"TX","postalCode":"75001","country":"USA","phone":"abc","method":"drone"},
				 "payment":{"cardName":"Ann","cardNumber":"12","expiry":"13/99","cvc":"1"}}
				""";
		mvc.perform(post(V1 + "/orders").header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON).content(bad))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors[?(@.field=='shipping.fullName')]").isNotEmpty())
				.andExpect(jsonPath("$.fieldErrors[?(@.field=='shipping.phone')]").isNotEmpty())
				.andExpect(jsonPath("$.fieldErrors[?(@.field=='shipping.method')]").isNotEmpty())
				.andExpect(jsonPath("$.fieldErrors[?(@.field=='payment.cardNumber')]").isNotEmpty())
				.andExpect(jsonPath("$.fieldErrors[?(@.field=='payment.expiry')]").isNotEmpty())
				.andExpect(jsonPath("$.fieldErrors[?(@.field=='payment.cvc')]").isNotEmpty())
				.andExpect(jsonPath("$.fieldErrors[?(@.field=='items[0].quantity')]").isNotEmpty());
	}

	@Test
	void emptyCartAndUnknownBookAndExpiredCardAreRejected() throws Exception {
		String token = signup("Biz", "biz@example.com", "secret1");
		String empty = """
				{"items":[],"shipping":{"fullName":"A","address":"B","city":"C","state":"D","postalCode":"1","country":"E","phone":"55555","method":"standard"},
				 "payment":{"cardName":"A","cardNumber":"4242424242424242","expiry":"12/99","cvc":"123"}}
				""";
		mvc.perform(post(V1 + "/orders").header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON).content(empty))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Order must contain at least one item"));

		mvc.perform(post(V1 + "/orders").header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON).content(orderBody(99999, 1, "4242424242424242", futureExpiry())))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", containsString("does not exist")));

		mvc.perform(post(V1 + "/orders").header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON).content(orderBody(firstBookId(), 1, "4242424242424242", "01/20")))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Card has expired"));

		mvc.perform(post(V1 + "/orders").header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON).content(orderBody(firstBookId(), 1, "1234 5678 9012", futureExpiry())))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Card number must be 16 digits"));
	}

	// ---------- docs + CORS ----------

	@Test
	void swaggerAndOpenApiDocsArePublicAndVersioned() throws Exception {
		MvcResult docs = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("Ecom Bookstore API")).andReturn();
		String body = docs.getResponse().getContentAsString();
		org.junit.jupiter.api.Assertions.assertTrue(body.contains("/api/v1/orders"));
		org.junit.jupiter.api.Assertions.assertTrue(body.contains("bearerAuth"));
		mvc.perform(get("/v3/api-docs/v1")).andExpect(status().isOk());
		mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
	}

	@Test
	void corsAllowsTheFrontendOriginOnly() throws Exception {
		mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options(V1 + "/orders")
				.header("Origin", "http://localhost:3000")
				.header("Access-Control-Request-Method", "POST")
				.header("Access-Control-Request-Headers", "authorization,content-type"))
				.andExpect(status().isOk())
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
						.string("Access-Control-Allow-Origin", "http://localhost:3000"));
		mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options(V1 + "/orders")
				.header("Origin", "http://evil.example.com")
				.header("Access-Control-Request-Method", "POST"))
				.andExpect(status().isForbidden());
	}
}
