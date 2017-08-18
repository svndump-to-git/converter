package io.github.svndump_to_git.model.author;


public interface HostBasedPersonIdentProvider extends PersonIdentProvider {

    /*
     * Set the host email part.  All SVN authors will be come author@emailHostPart
     */
    void setEmailHostPart(String emailHostPart);
}
