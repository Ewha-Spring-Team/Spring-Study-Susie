package firstproject.dto;

import firstproject.entity.Article;
import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class ArticleForm {
    private String id;
    private String title;
    private String content;

    public Article toEntity() {
        return new Article(id, title, content);
    }
}


