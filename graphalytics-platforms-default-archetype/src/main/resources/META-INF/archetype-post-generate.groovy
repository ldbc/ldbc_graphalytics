// Modify file permission to executable for all files in /bin directory.
def binPath = new File( request.getOutputDirectory(), request.getArtifactId()+ "/bin");

binPath.traverse {
    it.setExecutable(true, false);
}