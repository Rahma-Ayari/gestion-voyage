package Utils;

import Entite.Administrateur;
import Entite.Utilisateur;

/**
 * 🎭 Gestion avancée de la session avec système de rôles
 * 
 * ✨ Fonctionnalités :
 * - Gestion des rôles (ADMIN, USER)
 * - Stockage de l'ID utilisateur
 * - Logs détaillés pour débogage
 * - Vérification des permissions
 * - État de la session facile à accéder
 */
public final class SessionManager {
    
    // ═══════════════════════════════════════════════════════════════
    // Variables d'état de session
    // ═══════════════════════════════════════════════════════════════
    private static Utilisateur    currentUser;
    private static Administrateur currentAdmin;
    private static String         userRole;      // "ADMIN", "USER", ou "NONE"
    private static int            userId;        // ID unique de l'utilisateur
    private static boolean        isAuthenticated;

    private SessionManager() {
        // Utilitaire statique
    }

    // ═══════════════════════════════════════════════════════════════
    // 📝 SETTERS - Définir la session
    // ═══════════════════════════════════════════════════════════════

    /**
     * Définie l'administrateur connecté
     * @param admin L'objet Administrateur
     */
    public static void setCurrentAdmin(Administrateur admin) {
        currentAdmin = admin;
        currentUser = admin;
        userId = admin.getIdUtilisateur();
        userRole = "ADMIN";
        isAuthenticated = true;
        
        logSessionChange("ADMIN", admin.getEmail(), admin.getIdUtilisateur());
    }

    /**
     * Définie l'utilisateur normal connecté
     * @param user L'objet Personne/Utilisateur
     */
    public static void setCurrentUser(Utilisateur user) {
        currentUser = user;
        userId = user.getIdUtilisateur();
        userRole = "USER";
        isAuthenticated = true;
        currentAdmin = null;  // Vider l'admin s'il y en avait un
        
        logSessionChange("USER", user.getEmail(), user.getIdUtilisateur());
    }

    /**
     * Définie le rôle manuellement
     * @param role "ADMIN" ou "USER"
     */
    public static void setUserRole(String role) {
        userRole = role;
        System.out.println("📌 Rôle défini à : " + role);
    }

    // ═══════════════════════════════════════════════════════════════
    // 📖 GETTERS - Récupérer l'état de la session
    // ═══════════════════════════════════════════════════════════════

    /**
     * Récupère l'utilisateur actuellement connecté
     * @return L'objet Utilisateur (peut être un Admin ou un User normal)
     */
    public static Utilisateur getCurrentUser() {
        return currentUser;
    }

    /**
     * Récupère l'administrateur actuellement connecté
     * @return L'objet Administrateur, ou null si ce n'est pas un admin
     */
    public static Administrateur getCurrentAdmin() {
        return currentAdmin;
    }

    /**
     * Récupère l'ID de l'utilisateur connecté
     * @return L'ID unique de l'utilisateur
     */
    public static int getUserId() {
        return userId;
    }

    /**
     * Récupère le rôle de l'utilisateur
     * @return "ADMIN", "USER", ou "NONE"
     */
    public static String getUserRole() {
        return userRole;
    }

    /**
     * Récupère le nom complet de l'utilisateur (si disponible)
     * @return Prénom + Nom, ou email si pas disponible
     */
    public static String getUserName() {
        if (currentUser == null) return "Invité";
        
        // Si c'est une Personne avec nom et prénom
        if (currentUser instanceof Entite.Personne) {
            Entite.Personne p = (Entite.Personne) currentUser;
            return p.getPrenom() + " " + p.getNom();
        }
        
        // Sinon, retourner l'email
        return currentUser.getEmail();
    }

    /**
     * Récupère l'email de l'utilisateur connecté
     * @return L'email, ou null si aucun utilisateur
     */
    public static String getUserEmail() {
        return currentUser != null ? currentUser.getEmail() : null;
    }

    // ═══════════════════════════════════════════════════════════════
    // 🔐 VÉRIFICATIONS - Vérifier les permissions
    // ═══════════════════════════════════════════════════════════════

    /**
     * Vérifie si un utilisateur est connecté
     * @return true si un utilisateur quelconque est connecté
     */
    public static boolean isAuthenticated() {
        return isAuthenticated && currentUser != null;
    }

    /**
     * Vérifie si c'est un administrateur
     * @return true si le rôle est "ADMIN"
     */
    public static boolean isAdmin() {
        return "ADMIN".equals(userRole) && currentAdmin != null;
    }

    /**
     * Vérifie si c'est un utilisateur normal
     * @return true si le rôle est "USER"
     */
    public static boolean isUser() {
        return "USER".equals(userRole) && currentAdmin == null;
    }

    /**
     * Vérifie qu'on est authentifié et qu'on a le bon rôle
     * @param requiredRole Le rôle requis ("ADMIN" ou "USER")
     * @return true si l'utilisateur a le bon rôle
     */
    public static boolean hasRole(String requiredRole) {
        if (!isAuthenticated()) return false;
        return requiredRole.equals(userRole);
    }

    // ═══════════════════════════════════════════════════════════════
    // 🔓 DÉCONNEXION - Vider la session
    // ═══════════════════════════════════════════════════════════════

    /**
     * Vide complètement la session (déconnexion)
     */
    public static void clearSession() {
        String email = currentUser != null ? currentUser.getEmail() : "Unknown";
        
        currentUser = null;
        currentAdmin = null;
        userRole = "NONE";
        userId = 0;
        isAuthenticated = false;
        
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("🚪 DÉCONNEXION");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("👤 Utilisateur : " + email);
        System.out.println("✅ Session vidée avec succès");
        System.out.println("╚════════════════════════════════════════╝\n");
    }

    // ═══════════════════════════════════════════════════════════════
    // 📊 DÉBOGAGE - Afficher l'état complet
    // ═══════════════════════════════════════════════════════════════

    /**
     * Affiche l'état complet de la session (pour débogage)
     */
    public static void printSessionStatus() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("📊 ÉTAT DE LA SESSION");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("✅ Authentifié : " + (isAuthenticated ? "OUI" : "NON"));
        System.out.println("🎭 Rôle : " + userRole);
        System.out.println("🆔 ID Utilisateur : " + userId);
        if (currentUser != null) {
            System.out.println("📧 Email : " + currentUser.getEmail());
            System.out.println("👤 Nom : " + getUserName());
        }
        if (currentAdmin != null) {
            System.out.println("🔐 Statut Admin : ACTIVÉ");
        }
        System.out.println("╚════════════════════════════════════════╝\n");
    }

    /**
     * Affiche les permissions de l'utilisateur
     */
    public static void printPermissions() {
        System.out.println("\n┌────────────────────────────────────────┐");
        System.out.println("🔐 PERMISSIONS DE " + userRole);
        System.out.println("├────────────────────────────────────────┤");
        
        if (isAdmin()) {
            System.out.println("✅ Accès Dashboard Admin");
            System.out.println("✅ Gestion des utilisateurs");
            System.out.println("✅ Gestion des offres");
            System.out.println("✅ Statistiques et rapports");
        } else if (isUser()) {
            System.out.println("✅ Consulter les offres de voyage");
            System.out.println("✅ Filtrer par destination");
            System.out.println("✅ Voir détails des voyages");
            System.out.println("✅ Configurer un voyage");
        } else {
            System.out.println("❌ Aucune permission");
        }
        System.out.println("└────────────────────────────────────────┘\n");
    }

    // ═══════════════════════════════════════════════════════════════
    // 🔧 UTILITAIRES PRIVÉS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Log interne pour tracer les changements de session
     */
    private static void logSessionChange(String role, String email, int id) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("✅ SESSION ÉTABLIE");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("🎭 Rôle : " + role);
        System.out.println("📧 Email : " + email);
        System.out.println("🆔 ID : " + id);
        System.out.println("╚════════════════════════════════════════╝\n");
    }

    // ═══════════════════════════════════════════════════════════════
    // 🎯 MÉTHODES DE VÉRIFICATION AVANCÉES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Vérifie que l'utilisateur a les permissions pour accéder à une ressource
     * @param userId L'ID de la ressource
     * @return true si l'utilisateur peut y accéder
     */
    public static boolean canAccessUserResource(int userId) {
        // Un admin peut accéder à n'importe quelle ressource
        if (isAdmin()) return true;
        
        // Un user ne peut accéder qu'à ses propres ressources
        return isUser() && SessionManager.userId == userId;
    }

    /**
     * Crée un résumé rapide de la session
     * @return String avec les infos principales
     */
    public static String getSessionSummary() {
        if (!isAuthenticated()) {
            return "Aucune session active";
        }
        return String.format("[%s] %s (%d)", userRole, getUserEmail(), userId);
    }

    /**
     * Exporte l'état de la session en JSON (utile pour les logs)
     * @return String au format JSON
     */
    public static String exportSessionAsJson() {
        return String.format(
            "{\"authenticated\":%b,\"role\":\"%s\",\"userId\":%d,\"email\":\"%s\"}",
            isAuthenticated, userRole, userId, getUserEmail()
        );
    }
}