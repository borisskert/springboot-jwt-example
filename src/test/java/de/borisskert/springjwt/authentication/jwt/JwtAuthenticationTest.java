package de.borisskert.springjwt.authentication.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class JwtAuthenticationTest {

    @Nested
    class WhenCreate {
        private Authentication created;

        @BeforeEach
        public void setup() throws Exception {
            created = JwtAuthentication.of("my principal", Set.of("ADMIN", "USER"));
        }

        @Test
        public void shouldProvidePrincipal() throws Exception {
            assertThat(created.getPrincipal(), is(equalTo("my principal")));
        }

        @Test
        public void shouldProvideAuthorities() throws Exception {
            Set<String> actualAuthorities = created.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toUnmodifiableSet());

            assertThat(actualAuthorities, is(equalTo(Set.of("ROLE_ADMIN", "ROLE_USER"))));
        }

        /**
         * The Authentication Object has to be serializable
         */
        @Nested
        class WhenSerialize {

            @Test
            public void shouldSerializeAndDeserialize() throws Exception {
                byte[] bytes = serialize(created);
                Authentication deserialized = deserialize(bytes);

                assertThat(deserialized, is(equalTo(created)));
            }

            private byte[] serialize(Authentication authentication) throws IOException {
                ByteArrayOutputStream stream = null;
                ObjectOutput output = null;

                try {
                    stream = new ByteArrayOutputStream();
                    output = new ObjectOutputStream(stream);

                    output.writeObject(authentication);

                    return stream.toByteArray();
                } finally {
                    if (output != null) output.close();
                    if (stream != null) stream.close();
                }
            }

            private Authentication deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
                ByteArrayInputStream stream = null;
                ObjectInput input = null;

                try {
                    stream = new ByteArrayInputStream(bytes);
                    input = new ObjectInputStream(stream);

                    Object object = input.readObject();

                    return (JwtAuthentication) object;
                } finally {
                    if (stream != null) stream.close();
                    if (input != null) input.close();
                }
            }
        }
    }
}
