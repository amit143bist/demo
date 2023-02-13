package codinpad.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import codinpad.model.Planet;

@Repository(value = "planetRepository")
public interface PlanetRepository extends CrudRepository<Planet, String> {

}