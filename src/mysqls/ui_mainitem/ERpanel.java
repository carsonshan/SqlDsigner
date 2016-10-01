package mysqls.ui_mainitem;

import mysqls.contanst.ConnectINFO;
import mysqls.contanst.ConnectINFOListener;
import mysqls.framework.PersistenceService;
import mysqls.framework.ToolBar;
import mysqls.graph.Graph;
import mysqls.sql.SQLEditPane;
import mysqls.sql.SQLlogPane;
import mysqls.sql.databaseserver2.MYtreeNodeDB;
import mysqls.sql.util.SQLCreator;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Created by jiang on 2016/10/1 0001.
 * 下面的面板
 */
public class ERpanel extends JPanel implements ConnectINFOListener{
    private static ERpanel me=null;
    private  GraphPanel aPanel=null;
    private  Graph pGraph = null;

    public static ERpanel getInstance() {
        if (me == null) {
            me=new ERpanel();
        }
        return me;
    }

    private ERpanel() {
        ConnectINFO.addLister(this);

        setLayout(new BorderLayout());
        if (ConnectINFO.getInstance().getDatabaseName() == null) {
            add(new JLabel("请选择数据库！！！"));
            return;
        }
        MYtreeNodeDB db = new MYtreeNodeDB(ConnectINFO.getInstance().getDatabaseName());
        StringBuilder builder = new StringBuilder();
        db.geTablesdata().stream().forEach(a -> builder.append(SQLCreator.create(a)));
        pGraph = PersistenceService.readSQL(builder.toString(), aPanel.aGraph);
//        aPanel.updateui();
        ToolBar sideBar = new ToolBar(pGraph);
        aPanel = new GraphPanel(pGraph, sideBar);
        add(sideBar, BorderLayout.EAST);
        add(aPanel,BorderLayout.CENTER);
    }


    @Override
    public void onchange(String name, Object news, Object oldies) {
        if (name.equals("databasename")) {
            if (aPanel == null) {
                removeAll();
                MYtreeNodeDB db = new MYtreeNodeDB(ConnectINFO.getInstance().getDatabaseName());
                StringBuilder builder = new StringBuilder();
                db.geTablesdata().stream().forEach(a -> builder.append(SQLCreator.create(a)));
                pGraph = PersistenceService.readSQL(builder.toString(), null);
//        aPanel.updateui();
                ToolBar sideBar = new ToolBar(pGraph);
                aPanel = new GraphPanel(pGraph, sideBar);
                add(sideBar, BorderLayout.EAST);
                add(aPanel, BorderLayout.CENTER);
                validate();
                updateUI();
            } else {

                MYtreeNodeDB db = new MYtreeNodeDB((String) news);
                StringBuilder builder = new StringBuilder();
                db.geTablesdata().stream().forEach(a -> builder.append(SQLCreator.create(a)));
                pGraph = PersistenceService.readSQL(builder.toString(), aPanel.aGraph);
                aPanel.aGraph = pGraph;
                aPanel.updateui();
                validate();
                updateUI();
            }
        }

    }
}
