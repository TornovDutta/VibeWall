package org.example.vibewall.service;

import org.example.vibewall.exception.PrincipalNotFollowException;
import org.example.vibewall.exception.UserNotFoundException;
import org.example.vibewall.model.Confession;

import java.util.List;

public interface ConfessionService  {
    Confession create(Confession confession, String username)
            throws UserNotFoundException, PrincipalNotFollowException;

    void delete(String id, String username);

    void update(String id, Confession updatedConfession, String username)
            throws PrincipalNotFollowException;

    List<Confession> showAll();
    Confession show(String id);
}
