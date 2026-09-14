//---------------------- milestone 3 ----------------------
package com.condotrack.backend.security;

import com.condotrack.backend.model.User;
import com.condotrack.backend.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.isActive()) {
            throw new UsernameNotFoundException("User is inactive");
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .disabled(!user.isActive())
                .authorities(user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getCode()))
                        .toList())
                .build();
    }
}


// //---------------------- milestone 4 ----------------------
// package com.condotrack.backend.security;

// import com.condotrack.backend.model.Permission;
// import com.condotrack.backend.model.Role;
// import com.condotrack.backend.model.User;
// import com.condotrack.backend.repository.UserRepository;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.ArrayList;
// import java.util.List;

// @Service
// public class CustomUserDetailsService implements UserDetailsService {

//     private final UserRepository userRepository;

//     public CustomUserDetailsService(UserRepository userRepository) {
//         this.userRepository = userRepository;
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public UserDetails loadUserByUsername(String username)
//             throws UsernameNotFoundException {

//         User user = userRepository.findByEmailIgnoreCase(username)
//                 .orElseThrow(() ->
//                         new UsernameNotFoundException("User not found"));

//         if (!user.isActive()) {
//             throw new UsernameNotFoundException("User is inactive");
//         }

//         List<SimpleGrantedAuthority> authorities = new ArrayList<>();

//         for (Role role : user.getRoles()) {

//             // Role authority
//             authorities.add(
//                     new SimpleGrantedAuthority(
//                             "ROLE_" + role.getCode()
//                     )
//             );

//             // Permission authorities
//             for (Permission permission : role.getPermissions()) {
//                 authorities.add(
//                         new SimpleGrantedAuthority(
//                                 "PERM_" + permission.getCode()
//                         )
//                 );
//             }
//         }

//         return org.springframework.security.core.userdetails.User
//                 .withUsername(user.getEmail())
//                 .password(user.getPasswordHash())
//                 .disabled(!user.isActive())
//                 .authorities(authorities)
//                 .build();
//     }
// }
