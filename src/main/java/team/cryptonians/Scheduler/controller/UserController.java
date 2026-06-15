package team.cryptonians.Scheduler.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import team.cryptonians.Scheduler.dto.UserDto;
import team.cryptonians.Scheduler.model.User;
import team.cryptonians.Scheduler.service.JwtService;
import team.cryptonians.Scheduler.service.UserService;

@RestController
@CrossOrigin("*")
public class UserController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtService jwtService;

    @Autowired
    UserService service;

    @PostMapping("/login")
    public String login(@RequestBody UserDto userdto){
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userdto.getUsername(),userdto.getPassword())
        );

        if(auth.isAuthenticated()){
            return jwtService.generateToken(userdto.getUsername());
        }

        return "Failed";
    }

    @PostMapping("/register")
    public User register(@RequestBody User user){
        return service.saveUser(user);
    }

}
