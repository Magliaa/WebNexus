package it.unimib.sd2024;

import it.unimib.sd2024.objs.Domain;
import it.unimib.sd2024.objs.Domains;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Domain domain = new Domain();

        Map<String, Domain> domainsMap = new HashMap<>();
        domainsMap.put("domain0.com", domain);

        Domains domains = new Domains();
        domains.domains = domainsMap;



    }
}