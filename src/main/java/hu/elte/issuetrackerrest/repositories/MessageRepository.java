package hu.elte.issuetrackerrest.repositories;

import hu.elte.issuetrackerrest.entities.Message;
import org.springframework.data.repository.CrudRepository;

public interface MessageRepository extends CrudRepository<Message, Integer> {
    
}
