package br.com.sw2you.realmeet.domain.model;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Employee {
    @Column(name = "employee_name")
    String nome;

    @Column(name = "employee_email")
    String email;

    public Employee(Builder builder) {
        nome = builder.nome;
        email = builder.email;
    }

    public Employee() {}

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(nome, employee.nome) && Objects.equals(email, employee.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, email);
    }

    @Override
    public String toString() {
        return "Employee{ nome='" + nome + '\'' + ", email='" + email + '\'' + '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        String nome;

        String email;

        private Builder() {}

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Employee build() {
            return new Employee(this);
        }
    }
}
