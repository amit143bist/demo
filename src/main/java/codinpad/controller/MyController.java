package codinpad.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.assertj.core.util.IterableUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import codinpad.domain.*;
import codinpad.exception.ResourceNotFoundException;
import codinpad.model.Planet;
import codinpad.repository.PlanetRepository;

@RestController
public class MyController {

    @Autowired
    private PlanetRepository planetRepository;

    @PostConstruct
    public void loadAllPlanetsAtStart() {

        if (planetRepository.count() == 0) {
            loadAllPlanets();
        }
    }

    @PostMapping("/planets/load")
    public ResponseEntity<String> loadAllPlanets() {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("content-type", "application/graphql"); // maintain graphql

        // query is a grapql query wrapped into a String
        String query1 = "query Query {\n  allPlanets {\n    planets {\n      id\n      name\n      population\n      terrains\n      climates\n    }\n  }\n}";

        String URL = "https://swapi-graphql.netlify.app/.netlify/functions/index";

        ResponseEntity<PlanetResponse> response = restTemplate.postForEntity(URL, new HttpEntity<>(query1, headers),
                PlanetResponse.class);

        PlanetResponse planetResponse = response.getBody();
        if (planetResponse != null) {
            List<PlanetInformation> planets = planetResponse.getData().getAllPlanets().getPlanets();

            List<Planet> planetDataColl = planets.stream().map(planet -> new Planet(planet.getId(), planet.getName(),
                    planet.getPopulation(),
                    planet.getTerrains(),
                    planet.getClimates())).collect(Collectors.toList());

            planetRepository.saveAll(planetDataColl);
        }

        return new ResponseEntity<String>("Success", HttpStatus.OK);
    }

    @GetMapping("/planets/{planetId}")
    public ResponseEntity<PlanetInformation> findRecordId(@PathVariable String planetId) {

        return planetRepository.findById(planetId).map(recordIdEntry -> {

            return new ResponseEntity<PlanetInformation>(new PlanetInformation(
                    recordIdEntry.getPlanetId(), recordIdEntry.getName(),
                    recordIdEntry.getPopulation(),
                    recordIdEntry.getTerrains(),
                    recordIdEntry.getClimates()), HttpStatus.OK);
        }).orElseThrow(
                () -> new ResourceNotFoundException("No Record found in planets for " + planetId));

    }

    @GetMapping("/planets")
    public ResponseEntity<AllPlanets> findAllPlanets() {

        Iterable<Planet> planetIterable = planetRepository.findAll();
        if (IterableUtil.isNullOrEmpty(planetIterable)) {

            throw new ResourceNotFoundException("No Data exists");
        }

        List<PlanetInformation> planetInformationList = new ArrayList<PlanetInformation>();

        planetIterable.forEach(recordIdEntry -> {

            planetInformationList.add(new PlanetInformation(
                    recordIdEntry.getPlanetId(), recordIdEntry.getName(),
                    recordIdEntry.getPopulation(),
                    recordIdEntry.getTerrains(),
                    recordIdEntry.getClimates()));
        });

        AllPlanets allPlanets = new AllPlanets();
        allPlanets.setPlanets(planetInformationList);

        return new ResponseEntity<AllPlanets>(allPlanets, HttpStatus.OK);
    }

    @PostMapping("/planets")
    public ResponseEntity<PlanetInformation> savePlanets(@RequestBody PlanetInformation planetRequest) {

        Planet planet = new Planet(planetRequest.getId(), planetRequest.getName(),
                planetRequest.getPopulation(),
                planetRequest.getTerrains(),
                planetRequest.getClimates());

        Planet savedPlanet = planetRepository.save(planet);

        PlanetInformation savedData = new PlanetInformation(
                savedPlanet.getPlanetId(), savedPlanet.getName(),
                savedPlanet.getPopulation(),
                savedPlanet.getTerrains(),
                savedPlanet.getClimates());

        return new ResponseEntity<PlanetInformation>(savedData, HttpStatus.OK);
    }

    @PutMapping("/planets/{planetId}")
    public ResponseEntity<PlanetInformation> updatePlanet(@RequestBody PlanetInformation planetRequest,
            @PathVariable String planetId) {

        Planet planet = planetRepository.findById(planetId).get();
        if (StringUtils.hasText(planetRequest.getName())) {
            planet.setName(planetRequest.getName());
        }

        if (planetRequest.getPopulation() != null) {
            planet.setPopulation(planetRequest.getPopulation());
        }

        if (planetRequest.getClimates() != null) {
            planet.setClimates(planetRequest.getClimates());
        }

        if (planetRequest.getTerrains() != null) {
            planet.setTerrains(planetRequest.getTerrains());
        }

        Planet savedPlanet = planetRepository.save(planet);

        PlanetInformation savedData = new PlanetInformation(
                savedPlanet.getPlanetId(), savedPlanet.getName(),
                savedPlanet.getPopulation(),
                savedPlanet.getTerrains(),
                savedPlanet.getClimates());

        return new ResponseEntity<PlanetInformation>(savedData, HttpStatus.OK);
    }

    @DeleteMapping("/planets/{planetId}")
    public ResponseEntity<String> deletePlanet(@PathVariable String planetId) {

        planetRepository.deleteById(planetId);

        return new ResponseEntity<String>("Success", HttpStatus.OK);
    }

    @DeleteMapping("/planets")
    public ResponseEntity<String> deletePlanets() {

        planetRepository.deleteAll();

        return new ResponseEntity<String>("Success", HttpStatus.OK);
    }

}
