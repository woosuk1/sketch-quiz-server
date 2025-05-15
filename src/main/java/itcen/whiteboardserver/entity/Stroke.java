package itcen.whiteboardserver.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "strokes")
@Data
public class Stroke {

    @Id
    private String id; // MongoDB ObjectId

    private String roomId;
    private String userId;
    private String color;
    private int width;
    private List<Point> points;

    @CreatedDate
    private LocalDateTime createdAt;

    @Data
    public static class Point {
        private int x;
        private int y;
    }
}

