import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Importer extends JFrame {
    static Path songPath = Path.of(new File("rhythm/song").toURI());
    static ArrayList<RmSong> songList = new ArrayList<>();
    static HashMap<String,String> songMap = new HashMap<>();

    public Importer() throws HeadlessException, IOException, URISyntaxException {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1024, 720);
        initializeSongList();
        //主容器(边界布局)
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        setContentPane(contentPane);
        //主表
        JTable songTable = new JTable();
        contentPane.add(songTable,"Center");
        //底部按钮
        JButton impButton = new JButton("导入");
        JButton closeButton = new JButton("关闭");
        JButton refreshButton = new JButton("刷新");
        Box bottomButtons = Box.createHorizontalBox();
        bottomButtons.add(impButton);
        bottomButtons.add(closeButton);
        bottomButtons.add(refreshButton);
        contentPane.add(bottomButtons,"South");



    }

    public static void main(String[] args){
        try {
            new Importer().setVisible(true);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 初始化歌曲列表
     */
    public static void initializeSongList() throws IOException, URISyntaxException {
        //读取xlsx表格
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(
                new FileInputStream(
                        new File(
                                Importer.class.getResource("songs.xlsx").toURI()
                        )
                )
        );
        //遍历工作簿中的所有数据
        for (int i = 0; i < xssfWorkbook.getNumberOfSheets(); i++) {
            //读取第i个工作表
            System.out.println("读取第" + (i + 1) + "个sheet");
            XSSFSheet sheet = xssfWorkbook.getSheetAt(i);
            //获取最后一行的num，即总行数。此处从0开始
            int maxRow = sheet.getLastRowNum();
            for (int row = 1; row <= maxRow; row++) {
                //获取最后单元格num，即总单元格数 ***注意：此处从1开始计数***
                int maxRol = sheet.getRow(row).getLastCellNum();
                int index = Integer.parseInt(sheet.getRow(row).getCell(0).getStringCellValue());
                String songPath = sheet.getRow(row).getCell(1).getStringCellValue();
                String songName = sheet.getRow(row).getCell(2).getStringCellValue();
                String artistName = sheet.getRow(row).getCell(3).getStringCellValue();
                double bpm = Double.parseDouble(sheet.getRow(row).getCell(4).getStringCellValue());
                int songLength;
                try {
                    songLength = (int) ((new SimpleDateFormat("mm:ss").
                            parse(sheet.getRow(row).getCell(5).
                                    getStringCellValue()
                            ).getTime()+8*3600*1000)/1000);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                int key = 4;
                String[] diffName = {"easy","normal","hard"};
                int diffIndex=0;
                ArrayList<RmSong.Difficulty> diffs = new ArrayList<>();
                for (int cellNum = 6;cellNum< maxRol;){
                    for (int j = 1; j <= 3; j++) {
                        String levStr = sheet.getRow(row).getCell(cellNum++).getStringCellValue();
                        int level = Integer.parseInt(levStr.substring(levStr.indexOf(".") + 1));
                        int objCount = Integer.parseInt(sheet.getRow(row).getCell(cellNum++).getStringCellValue());
                        diffs.add(
                                new RmSong.Difficulty(diffName[diffIndex],key,level,objCount)
                        );
                        diffIndex++;
                    }
                    key++;
                    diffIndex=0;
                }
                RmSong song = new RmSong(index,songPath,songName,artistName,bpm,songLength,diffs);
                songList.add(song);
                songMap.put(songPath,songName);
            }
        }
    }


    static class RmSong {
        int index;
        String songPath;
        String songName;
        String artistName;
        double bpm;
        int songLength;
        ArrayList<Difficulty> diffs;

        public RmSong(int index,String songPath, String songName, String artistName, double bpm, int songLength, ArrayList<Difficulty> diffs) {
            this.index = index;
            this.songPath = songPath;
            this.songName = songName;
            this.artistName = artistName;
            this.bpm = bpm;
            this.songLength = songLength;
            this.diffs = diffs;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RmSong rmSong = (RmSong) o;
            return songPath.equals(rmSong.songPath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(songPath);
        }

        @Override
        public String toString() {
            return "RmSong{" +
                    "index=" + index +
                    ", songPath='" + songPath + '\'' +
                    ", songName='" + songName + '\'' +
                    ", artistName='" + artistName + '\'' +
                    ", bpm=" + bpm +
                    ", songLength=" + songLength +
                    ", diffs=" + diffs +
                    '}';
        }

        static class Difficulty{
            String name;
            int key;
            int level;
            int objCount;

            public Difficulty(String name, int key, int level, int objCount) {
                this.name = name;
                this.key = key;
                this.level = level;
                this.objCount = objCount;
            }

            @Override
            public String toString() {
                return "\n难度:"+key+"key " + name +" lv." + level + " 物量:"+ objCount ;
            }
        }
    }
}