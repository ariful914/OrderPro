package com.arif.smartordermanagement;

import com.arif.smartordermanagement.data.AuthRepository;
import com.arif.smartordermanagement.model.User;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AuthRepositoryTest {

    private AuthRepository authRepository;

    @Before
    public void setUp() {
        authRepository = new AuthRepository();
    }

    @Test
    public void login_withValidCredentials_returnsSuccessUser() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<User> resultUser = new AtomicReference<>();

        authRepository.login("alex.mercer@orderpro.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                resultUser.set(user);
                latch.countDown();
            }

            @Override
            public void onError(String errorMessage) {
                latch.countDown();
            }
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        User user = resultUser.get();
        assertNotNull(user);
        assertEquals("Alex Mercer", user.getName());
        assertEquals("alex.mercer@orderpro.com", user.getEmail());
        assertEquals(user, authRepository.getCurrentUser());
    }

    @Test
    public void login_withInvalidEmail_returnsError() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> errorRef = new AtomicReference<>();

        authRepository.login("invalid-email", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                latch.countDown();
            }

            @Override
            public void onError(String errorMessage) {
                errorRef.set(errorMessage);
                latch.countDown();
            }
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals("Please enter a valid email address", errorRef.get());
    }

    @Test
    public void logout_clearsCurrentUser() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        authRepository.login("alex.mercer@orderpro.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                latch.countDown();
            }

            @Override
            public void onError(String errorMessage) {
                latch.countDown();
            }
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertNotNull(authRepository.getCurrentUser());

        authRepository.logout();
        assertNull(authRepository.getCurrentUser());
    }
}
