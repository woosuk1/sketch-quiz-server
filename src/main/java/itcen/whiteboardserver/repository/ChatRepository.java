package itcen.whiteboardserver.repository;

import itcen.whiteboardserver.entity.Chat;
import itcen.whiteboardserver.entity.Stroke;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatRepository extends MongoRepository<Chat, String> {
    List<Chat> findByRoomIdOrderByCreatedAtAsc(String roomId);
}
