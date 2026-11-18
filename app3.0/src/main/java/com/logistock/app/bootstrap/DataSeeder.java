package com.logistock.app.bootstrap;

import com.logistock.app.model.Categoria;
import com.logistock.app.model.Plan;
import com.logistock.app.model.Producto;
import com.logistock.app.model.Rol;
import com.logistock.app.model.Usuario;
import com.logistock.app.repository.CategoriaRepository;
import com.logistock.app.repository.ProductoRepository;
import com.logistock.app.repository.RolRepository;
import com.logistock.app.repository.UsuarioRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Siembra roles, cuentas y catálogo de prueba al arrancar la aplicación.
 * Es imprescindible con el perfil H2 por defecto (base en memoria): al ser
 * "mem:", la base se crea vacía en cada reinicio, así que sin este seeder
 * no habría con qué usuario iniciar sesión. Con el perfil "mysql" también
 * corre, pero cada inserción está protegida con "si no existe" para no
 * duplicar datos ya cargados por database_saas.sql.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UsuarioRepository usuarioRepository,
                       RolRepository rolRepository,
                       CategoriaRepository categoriaRepository,
                       ProductoRepository productoRepository,
                       PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        Rol rolAdmin = rolRepository.findByNombre("ROLE_ADMIN")
                .orElseGet(() -> rolRepository.save(new Rol("ROLE_ADMIN")));
        Rol rolUser = rolRepository.findByNombre("ROLE_USER")
                .orElseGet(() -> rolRepository.save(new Rol("ROLE_USER")));

        Usuario admin = crearUsuarioSiNoExiste(
                "Administrador del Sistema", "admin@logistock.com", "admin123",
                Plan.PREMIUM, Set.of(rolAdmin));

        crearUsuarioSiNoExiste(
                "Administrador Secundario", "admin2@logistock.com", "admin123",
                Plan.PREMIUM, Set.of(rolAdmin));

        Usuario usuarioCliente = crearUsuarioSiNoExiste(
                "Usuario Invitado", "usuario@logistock.com", "admin123",
                Plan.BASICO, Set.of(rolUser));

        // Categorías globales del catálogo (usuario_id NULL -> visibles para todos).
        crearCategoriaGlobalSiNoExiste("Componentes");
        crearCategoriaGlobalSiNoExiste("Periféricos");
        crearCategoriaGlobalSiNoExiste("Hardware");
        crearCategoriaGlobalSiNoExiste("Otros");

        // Producto de ejemplo, propiedad del usuario cliente (multitenancy).
        if (usuarioCliente != null && productoRepository.findByUsuarioId(usuarioCliente.getId()).isEmpty()) {
            Categoria componentes = categoriaRepository.findByNombre("Componentes");
            Producto producto = new Producto();
            producto.setNombre("Disco Duro SSD 1TB");
            producto.setCantidad(10);
            producto.setPrecioCompra(new BigDecimal("150.00"));
            producto.setPrecioVenta(new BigDecimal("220.00"));
            producto.setCategoria(componentes);
            producto.setUsuario(usuarioCliente);
            productoRepository.save(producto);
        }
    }

    private Usuario crearUsuarioSiNoExiste(String nombreCompleto, String email, String passwordPlano,
                                            Plan plan, Set<Rol> roles) {
        return usuarioRepository.findByEmail(email).orElseGet(() -> {
            Usuario usuario = new Usuario();
            usuario.setNombreCompleto(nombreCompleto);
            usuario.setEmail(email);
            usuario.setPassword(passwordEncoder.encode(passwordPlano));
            usuario.setActivo(true);
            usuario.setPlan(plan);
            usuario.setRoles(roles);
            return usuarioRepository.save(usuario);
        });
    }

    private void crearCategoriaGlobalSiNoExiste(String nombre) {
        if (categoriaRepository.findByNombre(nombre) == null) {
            Categoria categoria = new Categoria();
            categoria.setNombre(nombre);
            categoriaRepository.save(categoria);
        }
    }
}
