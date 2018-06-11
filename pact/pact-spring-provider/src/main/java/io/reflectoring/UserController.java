package io.reflectoring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
public class UserController {

	private UserRepository userRepository;

	@Autowired
	public UserController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@PostMapping(path = "/user-service/users")
	public ResponseEntity<IdObject> createUser(@RequestBody @Valid User user) {
		User savedUser = this.userRepository.save(user);
		return ResponseEntity
				.status(201)
				.body(new IdObject(savedUser.getId()));
	}

	@PutMapping(path = "/user-service/users/{id}")
	public ResponseEntity<User> updateUser(@RequestBody @Valid User user, @PathVariable long id) {
		Optional<User> userFromDb = userRepository.findById(id);
		User userDB = userFromDb.get();
		userDB.updateFrom(user);
		userRepository.save(user);
		return ResponseEntity.ok(user);
	}

	@GetMapping(path = "/user-service/users/{id}")
	public ResponseEntity<User> getUser(@PathVariable("id") Long id) {
		return ResponseEntity.ok(userRepository.findById(id).get());
	}


}
