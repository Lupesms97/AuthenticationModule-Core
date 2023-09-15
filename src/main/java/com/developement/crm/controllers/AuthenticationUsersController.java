package com.developement.crm.controllers;

import com.developement.crm.dtos.MessageDto;
import com.developement.crm.dtos.ResponseLoginDto;
import com.developement.crm.dtos.UserLoginDto;
import com.developement.crm.dtos.UsersDto;
import com.developement.crm.exceptionHandlers.UserNotFoundException;
import com.developement.crm.model.UserModel;
import com.developement.crm.repositories.UsersRepository;
import com.developement.crm.service.TokenService;
import com.developement.crm.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import java.util.HashMap;


@RestController
@RequestMapping("/auth")
public class AuthenticationUsersController {

    @Autowired
    private UsersService usersService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder bCryptPasswordEncoder;

    @Autowired
    UsersRepository usersRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserLoginDto data){
//        HashMap mensagem = new HashMap();
        try {
            var userNamePassword = new UsernamePasswordAuthenticationToken(data.getLogin(), data.getPassword());
            var authentication = authenticationManager.authenticate(userNamePassword);

            var token = tokenService.generateToken((UserModel) authentication.getPrincipal());
            String message = "Usario logado com sucesso";
            return ResponseEntity.ok().body(new ResponseLoginDto(token, message));

        } catch (UserNotFoundException e) {
            MessageDto message = new MessageDto("Invalid username or password");
//            mensagem.put("mensagem", "Invalid username or password"+ e.getMessage());
            return ResponseEntity.badRequest().body(message);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid UsersDto user){
        HashMap mensagem = new HashMap();

        try {
            UserDetails userDetails = usersRepository.findByLogin(user.getLogin());
            if (userDetails != null) {
                mensagem.put("mensagem", "login já castrato");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(mensagem);
            }else {
                user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
                usersService.creatNewUser(UsersDto.convertToUserModel(user));

                mensagem.put("mensagem", "usuario cadastrado com sucesso");

                return ResponseEntity.status(HttpStatus.CREATED).body(mensagem);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        }

    }

}