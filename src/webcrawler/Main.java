package webcrawler;

public class Main {

    public static void main(String[] args) {
        Crawler crawler = new Crawler(10);
        crawler.findNGrams("http://www.glprogramming.com/red/");
    }
}
