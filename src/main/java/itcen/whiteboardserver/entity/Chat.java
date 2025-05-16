package itcen.whiteboardserver.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mongodb.lang.NonNull;
import com.mongodb.lang.Nullable;
import itcen.whiteboardserver.util.ChatType;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "chats")
@Data
public class Chat {
    @Id
    private String id;
    private String roomId;
    private String userId;
    private String message;
    private ChatType chatType;

    @CreatedDate
    private LocalDateTime createdAt;
}
