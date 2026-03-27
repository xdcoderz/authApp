package com.xdcoder.authApp;

import com.xdcoder.authApp.auth.config.AppConstants;
import com.xdcoder.authApp.auth.entities.Role;
import com.xdcoder.authApp.auth.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.UUID;

@SpringBootApplication
public class AuthAppApplication implements CommandLineRunner {

	@Autowired
	private RoleRepository roleRepository;

	public static void main(String[] args) {

		SpringApplication.run(AuthAppApplication.class, args);


	}
	@Override
	public void run(String... args) throws Exception {

		//we will create some default user role
		//admin
		//user
		roleRepository.findByName("ROLE_"+AppConstants.ROLE_ADMIN).ifPresentOrElse(role -> {
			System.out.println("Admin role already exists in the database: "+ role.getName());

		},() -> {
			Role role = new Role();
			role.setName("ROLE_"+AppConstants.ROLE_ADMIN);
			role.setId(UUID.randomUUID());
			roleRepository.save(role);
		});

		roleRepository.findByName("ROLE_USER").ifPresentOrElse(role -> {
			System.out.println("User role already exists in the database: "+ role.getName());

		},() -> {
			Role role = new Role();
			role.setName("ROLE_"+AppConstants.ROLE_USER);
			role.setId(UUID.randomUUID());
			roleRepository.save(role);
		});
	}

}
