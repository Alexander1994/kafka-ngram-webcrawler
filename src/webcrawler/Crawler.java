package webcrawler;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

class Crawler
{
    private final int maxPagesToSearch;
    private Set<String> pagesVisited = new HashSet<String>();
    private List<String> pagesToVisit = new LinkedList<String>();

    Crawler(int maxPagesCount) {
        this.maxPagesToSearch = maxPagesCount;
    }

    void findNGrams(String url) {

        TextProducer textProducer = new TextProducer("web-page-text");
        int i = 0;
        String docText;
        while (this.pagesVisited.size() < this.maxPagesToSearch) {
            String currentUrl;
            CrawlerBranch branch = new CrawlerBranch();
            if (this.pagesToVisit.isEmpty()) {
                currentUrl = url;
                this.pagesVisited.add(url);
            } else {
                currentUrl = this.nextUrl();
            }
            if (branch.crawl(currentUrl)) {
                docText = branch.getDocumentText();
                textProducer.send(i++, docText);

                this.pagesToVisit.addAll(branch.getLinks());
            }
        }
        textProducer.close();
        System.out.println("\n**Done** Visited " + this.pagesVisited.size() + " web page(s)");
    }

    private String nextUrl() {
        String nextUrl;
        do {
            nextUrl = this.pagesToVisit.remove(0);
        } while(this.pagesVisited.contains(nextUrl));
        this.pagesVisited.add(nextUrl);
        return nextUrl;
    }
}