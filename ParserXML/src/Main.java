import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

public class Main {
    public static void main(String[] args) {
        String xmlFile = "C:\\Users\\kessk\\OneDrive\\Рабочий стол\\TP\\XMLParser\\ParserXML\\src\\XMLFile.xml";
        List<Book> books = parse(xmlFile);

        for (Book book : books) {
            System.out.println(book);
        }

        String xsdFile = "C:\\Users\\kessk\\OneDrive\\Рабочий стол\\TP\\XMLParser\\ParserXML\\src\\XSDFile.xsd";
        validateXML(xmlFile, xsdFile);

        String outputXmlFile = "C:\\Users\\kessk\\OneDrive\\Рабочий стол\\TP\\XMLParser\\ParserXML\\src\\OutputXMLFile.xml";
        serializeBooks(books, outputXmlFile);
    }

    public static List<Book> parse(String filename) {
        List<Book> bookList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("<book")) {
                    bookList.add(parseBook(br, line));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bookList;
    }

    private static Book parseBook(BufferedReader br, String line) throws IOException {
        Book book = new Book();
        String id = line.substring(line.indexOf("id=\"") + 4, line.indexOf("\"", line.indexOf("id=\"") + 4));
        book.setId(Integer.parseInt(id));

        String innerLine;
        while ((innerLine = br.readLine()) != null) {
            innerLine = innerLine.trim();
            if (innerLine.startsWith("</book>")) {
                break;
            }
            if (innerLine.startsWith("<title>")) {
                book.setTitle(getTagContent(innerLine, "title"));
            } else if (innerLine.startsWith("<author>")) {
                book.setAuthor(getTagContent(innerLine, "author"));
            } else if (innerLine.startsWith("<year>")) {
                book.setYear(Integer.parseInt(getTagContent(innerLine, "year")));
            } else if (innerLine.startsWith("<genre>")) {
                book.setGenre(getTagContent(innerLine, "genre"));
            } else if (innerLine.startsWith("<price")) {
                String currency = getAttributeValue(innerLine, "currency");
                String priceValue = innerLine.substring(innerLine.indexOf(">") + 1, innerLine.indexOf("</price>"));
                Book.Price price = new Book.Price();
                price.setValue(Double.parseDouble(priceValue));
                price.setCurrency(currency);
                book.setPrice(price);
                System.out.println("Цены: " + price.getValue() + " " + price.getCurrency());
            } else if (innerLine.startsWith("<publisher>")) {
                book.setPublisher(parsePublisher(br));
            } else if (innerLine.startsWith("<translator>")) {
                book.setTranslator(getTagContent(innerLine, "translator"));
            } else if (innerLine.startsWith("<reviews>")) {
                book.setReviews(parseReviews(br));
            } else if (innerLine.startsWith("<awards>")) {
                book.setAwards(parseAwards(br));
            } else if (innerLine.startsWith("<isbn>")) {
                String isbn = getTagContent(innerLine, "isbn");
                book.setIsbn(isbn);
            }
        }
        return book;
    }

    private static Publisher parsePublisher(BufferedReader br) throws IOException {
        String line;
        String name = null, city = null, country = null;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("</publisher>")) {
                break;
            }
            if (line.startsWith("<name>")) {
                name = getTagContent(line, "name");
            } else if (line.startsWith("<address>")) {
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("</address>")) {
                        break;
                    }
                    if (line.startsWith("<city>")) {
                        city = getTagContent(line, "city");
                    } else if (line.startsWith("<country>")) {
                        country = getTagContent(line, "country");
                    }
                }
            }
        }
        return new Publisher(name, city, country);
    }

    private static List<Review> parseReviews(BufferedReader br) throws IOException {
        List<Review> reviews = new ArrayList<>();
        String line;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("</reviews>")) {
                break;
            }
            if (line.startsWith("<review>")) {
                reviews.add(parseReview(br));
            }
        }
        return reviews;
    }

    private static Review parseReview(BufferedReader br) throws IOException {
        String line;
        String user = null, comment = null;
        Integer rating = null;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("</review>")) {
                break;
            }
            if (line.startsWith("<user>")) {
                user = getTagContent(line, "user");
            } else if (line.startsWith("<rating>")) {
                rating = Integer.parseInt(getTagContent(line, "rating"));
            } else if (line.startsWith("<comment>")) {
                comment = getTagContent(line, "comment");
            }
        }
        return new Review(user, rating, comment);
    }

    private static List<String> parseAwards(BufferedReader br) throws IOException {
        List<String> awards = new ArrayList<>();
        String line;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("</awards>")) {
                break;
            }
            if (line.startsWith("<award>")) {
                awards.add(getTagContent(line, "award"));
            }
        }
        return awards;
    }

    private static String getTagContent(String line, String tagName) {
        int startIndex = line.indexOf("<" + tagName + ">") + tagName.length() + 2;
        int endIndex = line.indexOf("</" + tagName + ">");
        return line.substring(startIndex, endIndex);
    }

    private static String getAttributeValue(String line, String attributeName) {
        String attribute = attributeName + "=\"";
        int startIndex = line.indexOf(attribute) + attribute.length();
        int endIndex = line.indexOf("\"", startIndex);
        return line.substring(startIndex, endIndex);
    }

    private static void serializeBooks(List<Book> books, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("<library>\n");
            for (Book book : books) {
                writer.write("  <book id=\"" + book.getId() + "\">\n");
                writer.write("    <title>" + book.getTitle() + "</title>\n");
                writer.write("    <author>" + book.getAuthor() + "</author>\n");
                writer.write("    <year>" + book.getYear() + "</year>\n");
                writer.write("    <genre>" + book.getGenre() + "</genre>\n");
                if (book.getPrice() != null) {
                    writer.write("    <price currency=\"" + book.getPrice().getCurrency() + "\">");
                    writer.write("" + book.getPrice().getValue());
                    writer.write("</price>\n");
                }
                writer.write("    <reviews>\n");
                writer.write("    <isbn>" + book.getIsbn() + "</isbn>\n");
                for (Review review : book.getReviews()) {
                    writer.write("      <review>\n");
                    writer.write("        <user>" + review.getUser() + "</user>\n");
                    writer.write("        <rating>" + review.getRating() + "</rating>\n");
                    writer.write("        <comment>" + review.getComment() + "</comment>\n");
                    writer.write("      </review>\n");
                }
                writer.write("    </reviews>\n");
                if (book.getPublisher() != null) {
                    writer.write("    <publisher>\n");
                    writer.write("      <name>" + book.getPublisher().getName() + "</name>\n");
                    writer.write("      <address>\n");
                    writer.write("        <city>" + book.getPublisher().getCity() + "</city>\n");
                    writer.write("        <country>" + book.getPublisher().getCountry() + "</country>\n");
                    writer.write("      </address>\n");
                    writer.write("    </publisher>\n");
                }
                writer.write("    <awards>\n");
                for (String award : book.getAwards()) {
                    writer.write("      <award>" + award + "</award>\n");
                }
                writer.write("    </awards>\n");
                writer.write("  </book>\n");
            }
            writer.write("</library>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void validateXML(String xmlFile, String xsdFile) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(xsdFile));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new File(xmlFile)));
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}

class Book {
    private Integer id;
    private String title;
    private String author;
    private Integer year;
    private String genre;
    private Price price;
    private Publisher publisher;
    private List<Review> reviews = new ArrayList<>();
    private List<String> awards = new ArrayList<>();
    private String translator;
    private String isbn;

    public static class Price {
        private Double value;
        private String currency;

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public Integer getYear() {
        return year;
    }

    public String getGenre() {
        return genre;
    }

    public Price getPrice() {
        return price;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public List<String> getAwards() {
        return awards;
    }

    public String getTranslator() {
        return translator;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public void setAwards(List<String> awards) {
        this.awards = awards;
    }

    public void setTranslator(String translator) {
        this.translator = translator;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", year=" + year +
                ", genre='" + genre + '\'' +
                ", isbn='" + isbn + '\'' +
                ", price=" + (price != null ? price.getValue() + " " + price.getCurrency() : " ") +
                ", publisher=" + (publisher != null ? publisher : " ") +
                ", reviews=" + reviews +
                ", awards=" + awards +
                ", translator='" + translator + '\'' +
                '}';
    }
}

class Publisher {
    private String name;
    private String city;
    private String country;

    public Publisher(String name, String city, String country) {
        this.name = name;
        this.city = city;
        this.country = country;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public String toString() {
        return name + ", " + city + ", " + country;
    }
}

class Review {
    private String user;
    private Integer rating;
    private String comment;

    public Review(String user, Integer rating, String comment) {
        this.user = user;
        this.rating = rating;
        this.comment = comment;
    }

    public String getUser() {
        return user;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        return user + ": " + rating + "/5 - " + comment;
    }
}
