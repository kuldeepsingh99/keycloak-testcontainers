package com.portal.keycloak.security;


import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    String clientName;

    public CustomJwtGrantedAuthoritiesConverter(String clientName) {
        super();
        this.clientName = clientName;
    }

    @Override
    public <U> Converter<Jwt, U> andThen(Converter<? super Collection<GrantedAuthority>, ? extends U> after) {
        return Converter.super.andThen(after);
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Collection<GrantedAuthority> finalRoles = new ArrayList<>();
        extractRealmRoles(jwt.getClaim("realm_access"), finalRoles);
        extractClientRoles(jwt, finalRoles);

        return finalRoles;
    }

    private void extractRealmRoles(Object mapObject, Collection<GrantedAuthority> finalRoles) {
        if (mapObject instanceof Map) {
            Map<String, List<String>> rolesMap = (Map<String, List<String>>) mapObject;
            List<String> roles = rolesMap.get("roles");
            if (roles != null) {
                roles.forEach(role -> finalRoles.add(new SimpleGrantedAuthority(role)));
            }
        }
    }

    private void extractClientRoles(Jwt jwt, Collection<GrantedAuthority> finalRoles) {
        Map<String, Map<String, List<String>>> resourceAccessMap = jwt.getClaim("resource_access");

        if (resourceAccessMap != null) {
            Map<String, List<String>> clientAccessMap = resourceAccessMap.get(this.clientName);

            if (clientAccessMap != null) {
                List<String> roles = clientAccessMap.get("roles");

                if (roles != null) {
                    roles.forEach(role -> finalRoles.add(new SimpleGrantedAuthority(role)));
                }
            }
        }
    }
}
