package nl.tudelft.graphalytics.reporting.json;

import nl.tudelft.graphalytics.util.UuidGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wlngai on 10/14/16.
 */
public class ResultData {


    public String id = UuidGenerator.getRandomUUID("b", 6);
    public System system;
    public Benchmark benchmark;
    public Result result;

    public ResultData() {
        system = new System();
        benchmark = new Benchmark();
        result = new Result();
    }

    public class System {
        public Platform platform;
        public Environment environment;
        public Map<String, Tool> tool;

        public System() {
            tool = new HashMap<>();
        }

        public void addEnvironment(String name, String acronym, String version, String link) {
            environment = new Environment(name, acronym, version, link);
        }

        public void addMachine(String quantity, String cpu, String memory, String network, String storage) {
            environment.addMachine(quantity, cpu, memory, network, storage);
        }

        public void addPlatform(String name, String acronym, String version, String link) {
            platform = new Platform(name, acronym, version, link);
        }


        public void addTool(String name, String version, String link) {
            tool.put(name, new Tool(name, version, link));
        }
    }

    public class Platform {

        public Platform(String name, String acronym, String version, String link) {
            this.name = name;
            this.acronym = acronym;
            this.version = version;
            this.link = link;
        }

        public String name;
        public String acronym;
        public String version;
        public String link;
    }


    public class Environment {
        String name;
        String acronym;
        String version;
        String link;
        List<Machine> machines;

        public Environment(String name, String acronym, String version, String link) {
            this.name = name;
            this.acronym = acronym;
            this.version = version;
            this.link = link;
            machines = new ArrayList<>();
        }

        public void addMachine(String quantity, String cpu, String memory, String network, String storage) {
            machines.add(new Machine(quantity, cpu, memory, network, storage));
        }
    }

    public class Machine {

        public Machine(String quantity, String cpu, String memory, String network, String storage) {
            this.quantity = quantity;
            this.cpu = cpu;
            this.memory = memory;
            this.network = network;
            this.storage = storage;
        }

        String quantity;
        String cpu;
        String memory;
        String network;
        String storage;
    }


    public class Benchmark {
        String type = "";
        String name = "";
        String target_scale = "";
        String duration = "";
        Map<String, Resource> resources;
        Output output;
        Validation validation;


        public Benchmark() {
            resources = new HashMap<>();
        }

        public void addType(String type) {
            this.type = type;
        }

        public void addName(String name) {
            this.name = name;
        }


        public void addTargetScale(String targetScale) {
            this.target_scale = targetScale;
        }

        public void addValidation(String enabled, String path) {
            this.validation = new Validation(enabled, path);
        }

        public void addOutput(String enabled, String path) {
            this.output = new Output(enabled, path);
        }

        public void addResource(String name, String baseline, String scalability) {
            resources.put(name, new Resource(name, baseline, scalability));
        }

        public void addDuration(String duration) {
            this.duration = duration;
        }

    }

    public class Output {
        public Output(String required, String directory) {
            this.required = required;
            this.directory = directory;
        }

        String required;
        String directory;
    }

    public class Validation {

        public Validation(String required, String directory) {
            this.required = required;
            this.directory = directory;
        }

        String required;
        String directory;
    }

    public class Resource {

        public Resource(String name, String baseline, String scalability) {
            this.name = name;
            this.baseline = baseline;
            this.scalability = scalability;
        }
        String name;
        String baseline;
        String scalability;
    }

    public class Result {
        Map<String, Experiment> experiments;
        Map<String, Job> jobs;
        Map<String, Run> runs;

        public Result() {
            experiments = new HashMap<>();
            jobs = new HashMap<>();
            runs = new HashMap<>();
        }

        public void addExperiments(String id, String type, List<String> jobs) {
            experiments.put(id, new Experiment(id, type, jobs));
        }

        public void addJob(String id, String algorithm, String dataset, String scale, String repetition, List<String> runs) {
            jobs.put(id, new Job(id, algorithm, dataset, scale, repetition, runs));
        }

        public void addRun(String id, String timestamp, String success, String makespan, String processingTime, String archiveLink) {
            runs.put(id, new Run(id, timestamp, success, makespan, processingTime, archiveLink));
        }
    }

    public class Experiment {
        String id;
        String type;
        List<String> jobs;

        public Experiment(String id, String type, List<String> jobs) {
            this.id = id;
            this.type = type;
            this.jobs = jobs;
        }
    }

    public class Job {
        String id;
        String algorithm;
        String dataset;
        String scale;
        String repetition;
        List<String> runs;

        public Job(String id, String algorithm, String dataset, String scale, String repetition, List<String> runs) {
            this.id = id;
            this.algorithm = algorithm;
            this.dataset = dataset;
            this.scale = scale;
            this.repetition = repetition;
            this.runs = runs;
        }
    }

    public class Run {
        String id;
        String timestamp;
        String success;
        String makespan;
        String processing_time;
        String archive_link;

        public Run(String id, String timestamp, String success, String makespan, String processingTime, String archiveLink) {
            this.id = id;
            this.timestamp = timestamp;
            this.success = success;
            this.makespan = makespan;
            this.processing_time = processingTime;
            this.archive_link = archiveLink;
        }
    }


    public class Tool {

        public Tool(String name, String version, String link) {
            this.name = name;
            this.version = version;
            this.link = link;
        }

        String name;
        String version;
        String link;
    }

}
