package firstproject.repository;

import org.springframework.data.repository.CrudRepository;
import firstproject.entity.Article;

public interface ArticleRepository extends CrudRepository<Article, Long>{
    ArrayList<Article> findall();
}
