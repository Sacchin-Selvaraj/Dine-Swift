//package com.dineswift.restaurant_service.service;
//
//
//import jakarta.transaction.Transactional;
//import org.modelmapper.ModelMapper;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//
//@Service
//@Transactional
//public class RestaurantService {
//
//    private final UserRepository userRepository;
//    private final BookingRepository bookingRepository;
//    private final ModelMapper modelMapper;
//    private final PasswordEncoder passwordEncoder;
//    private final UserCommonService userCommonService;
//    private final RoleRepository roleRepository;
//
//
//
//    public RestaurantService(UserRepository userRepository, BookingRepository bookingRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder, UserCommonService userCommonService, RoleRepository roleRepository) {
//        this.userRepository = userRepository;
//        this.bookingRepository = bookingRepository;
//        this.modelMapper = modelMapper;
//        this.passwordEncoder = passwordEncoder;
//        this.userCommonService = userCommonService;
//        this.roleRepository = roleRepository;
//    }
//
//    public UserDTO updateDetails(UserDetailsRequest userDetailsRequest, UUID userId) {
//
//        User user=userCommonService.findValidUser(userId);
//
//        updateUserFromRequest(user, userDetailsRequest);
//
//        userRepository.save(user);
//
//
//        return modelMapper.map(user,UserDTO.class);
//    }
//
//    private void updateUserFromRequest(User user, UserDetailsRequest request) {
//        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
//        if (request.getLastName() != null) user.setLastName(request.getLastName());
//        if (request.getDob() != null) user.setDob(request.getDob());
//        if (request.getAddress() != null) user.setAddress(request.getAddress());
//        if (request.getArea() != null) user.setArea(request.getArea());
//        if (request.getCity() != null) user.setCity(request.getCity());
//        if (request.getDistrict() != null) user.setDistrict(request.getDistrict());
//        if (request.getState() != null) user.setState(request.getState());
//        if (request.getCountry() != null) user.setCountry(request.getCountry());
//        if (request.getPincode() != null) user.setPincode(request.getPincode());
//    }
//
//
//    public Page<BookingDTO> getBookings(UUID userId, Integer pageNumber, Integer limit, BookingStatus bookingStatus, String sortField, String sortOrder) {
//
//        Set<String> allowedFields = Set.of("createdAt", "lastModifiedAt", "bookingStatus","tableBookingId","bookingTime");
//
//        if (!allowedFields.contains(sortField)) {
//            throw new IllegalArgumentException("Invalid sort field: " + sortField);
//        }
//
//        Sort sort=sortOrder.equalsIgnoreCase("asc")?Sort.by(sortField).ascending():Sort.by(sortField).descending();
//
//        Pageable pageable= PageRequest.of(pageNumber,limit,sort);
//
//        Page<Booking> bookings;
//        if (bookingStatus==null){
//            bookings=bookingRepository.findByUser_UserId(userId,pageable);
//        }else {
//            bookings=bookingRepository.findByUser_UserIdAndBookingStatus(userId,bookingStatus,pageable);
//        }
//
//        return bookings.map(booking -> modelMapper.map(booking,BookingDTO.class));
//    }
//
//    public void deactivateUser(UUID userId) {
//        User user=userCommonService.findValidUser(userId);
//
//        user.setIsActive(false);
//        userRepository.save(user);
//    }
//
//    public void updateUsername(UUID userId, UsernameUpdateRequest usernameRequest) {
//
//        validUsername(usernameRequest.getUsername(),userId);
//        User user=userCommonService.findValidUser(userId);
//        user.setUsername(usernameRequest.getUsername());
//        userRepository.save(user);
//    }
//
//    private void validUsername(String username, UUID userId) {
//        userRepository.findByUsername(username).ifPresent(
//                user -> {
//                    if (!user.getUserId().equals(userId)) {
//                        throw new UserException("Username was already taken");
//                    }
//                });
//    }
//
//    public UUID createUser(UserRequest userRequest) {
//        try {
//            verifyUser(userRequest);
//
//            User user=modelMapper.map(userRequest,User.class);
//
//            Cart cart=new Cart();
//
//            Role role=roleRepository.findByRoleName("USER").orElseThrow(
//                    ()-> new UserException("Role Not Found")
//            );
//
//            user.setRoles(Set.of(role));
//            user.setCart(cart);
//
//            userRepository.save(user);
//
//            UserDTO userDTO=modelMapper.map(user,UserDTO.class);
//
//
//            return authResponse;
//
//        } catch (DataIntegrityViolationException e){
//            throw new DataIntegrityViolationException(e.getLocalizedMessage());
//        }
//    }
//
//    private void verifyUser(UserRequest userRequest) {
//        if (userRequest==null){
//            throw new UserException("User Details not found");
//        }
//        if (userRepository.existsByUsername(userRequest.getUsername())) {
//            throw new UserException("Username already taken!");
//        }
//        if (userRepository.existsByEmail(userRequest.getEmail())) {
//            throw new UserException("Email already registered!");
//        }
//    }
//
//}
