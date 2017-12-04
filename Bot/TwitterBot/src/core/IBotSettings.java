package core;
import java.util.List;

public interface IBotSettings {

	String getAPI_KEY();

	void setAPI_KEY(String aPI_KEY);

	String getAPI_SECRET();

	void setAPI_SECRET(String aPI_SECRET);

	String getACCESS_TOKEN();

	void setACCESS_TOKEN(String aCCESS_TOKEN);

	String getACCESS_TOKEN_SECRET();

	void setACCESS_TOKEN_SECRET(String aCCESS_TOKEN_SECRET);

	int getTWEET_DELAY();

	void setTWEET_DELAY(int tWEET_DELAY);

	List<String> getQUERIES();

	void setQUERIES(List<String> qUERIES);

}