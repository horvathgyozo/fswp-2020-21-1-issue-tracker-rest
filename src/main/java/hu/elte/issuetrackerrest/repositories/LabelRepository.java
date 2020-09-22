package hu.elte.issuetrackerrest.repositories;

import hu.elte.issuetrackerrest.entities.Label;
import org.springframework.data.repository.CrudRepository;

public interface LabelRepository extends CrudRepository<Label, Integer> {
    
}
