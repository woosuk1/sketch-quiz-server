package itcen.whiteboardserver.repository;

import itcen.whiteboardserver.entity.Stroke;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StrokeRepository extends MongoRepository<Stroke, String> {
    List<Stroke> findByRoomId(String roomId);
}


