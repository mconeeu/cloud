import eu.mcone.cloud.core.api.world.CloudWorldManager;

public class main {

    public static void main(String[] args) {
        CloudWorldManager cloudWorldManager = new CloudWorldManager("http://localhost:5000");
        cloudWorldManager.download("idBfG880", new int[]{1, 0, 0}, "E:\\test\\", "test", System.out::println);
        System.out.println(cloudWorldManager.getWorld("idBfG881"));
    }
}
