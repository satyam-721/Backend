package team.cryptonians.Scheduler.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import team.cryptonians.Scheduler.repo.UserRepo;
import team.cryptonians.Scheduler.model.User;

@Service
public class MyUserDetailService implements UserDetailsService {

    @Autowired
    UserRepo repo;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = repo.findByUsername(username);
        if (user == null){
            System.out.println("404: "+username+" Not found");
            throw new UsernameNotFoundException(username);

        }

        System.out.println(username + "authenticated");

        return new UserPrinciple(user);

    }

}
