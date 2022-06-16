package uz.abbos.phoneshop.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import uz.abbos.phoneshop.configuration.JwtTokenUtil;
import uz.abbos.phoneshop.dto.AuthDto;
import uz.abbos.phoneshop.dto.RegisterDto;
import uz.abbos.phoneshop.entity.User;
import uz.abbos.phoneshop.entity.UserRole;
import uz.abbos.phoneshop.enumm.Role;
import uz.abbos.phoneshop.exception.BadRequestException;
import uz.abbos.phoneshop.repository.UserRepository;
import uz.abbos.phoneshop.repository.UserRoleRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
public class AuthService {


    private MessageService messageService;
    private JwtTokenUtil jwtTokenUtil;
    private UserRoleRepository userRoleRepository;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    public String register(RegisterDto registerDto) {
        Optional optional = userRepository.findByEmailOrContactAndDeletedAtIsNull(registerDto.getEmail(), registerDto.getContact());

        if(optional.isPresent()) throw new BadRequestException("User already exist");

        User user = new User();
        user.setEmail(registerDto.getEmail());
        user.setContact(registerDto.getContact());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setStatus(false);
        user.setCreatedAt(LocalDateTime.now());

        UserRole userRole = new UserRole();
        userRole.setRole(Role.ADMIN);
        userRole.setStatus(true);
        userRoleRepository.save(userRole);
        userRepository.save(user);
        String token = jwtTokenUtil.generateAccessToken(user.getEmail(),user.getId());
        String link = "http://localhost:8080/auth/verification" + token;
        String  content = String.format("Please click %s for verification", link);

        try {
            messageService.send(user.getEmail(), "Isystem shop uz verification", content);
        }catch (Exception e) {
            userRepository.delete(user);
        }
        return "Please got to " + registerDto.getEmail() + " and verification" ;
    }

    public AuthDto login(AuthDto authDto) {
        Optional<User> optional = userRepository.findByEmailAndPasswordAndDeletedAtIsNull(authDto.getUsername(), authDto.getPassword());
        if(optional.isEmpty()) {
            throw new BadRequestException("User not found");
        }

        User user = optional.get();
        String token = jwtTokenUtil.generateAccessToken(user.getEmail(), user.getId());
        authDto.setToken(token);
        return authDto;
    }

    public boolean verification(String token) {
        return false;
    }
}




