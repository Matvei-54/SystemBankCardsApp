package com.example.bankcards.entity.app_class;

import com.example.bankcards.entity.CustomerEntity;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Setter
public class CustomerDetails implements UserDetails {

    private CustomerEntity customerEntity;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return customerEntity.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return customerEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return customerEntity.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return customerEntity.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return customerEntity.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return customerEntity.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return customerEntity.isEnabled();
    }
}
