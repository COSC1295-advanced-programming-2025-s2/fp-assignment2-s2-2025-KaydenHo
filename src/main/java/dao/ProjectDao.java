	package dao;
	
	import java.io.IOException;
	import java.util.List;
	import model.Project;
	
	public interface ProjectDao {
	    List<Project> loadAll() throws IOException;
	}
