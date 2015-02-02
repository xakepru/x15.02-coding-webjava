package com.mycompany.fileman;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.util.FilesystemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.io.File;
import java.util.Arrays;
import java.util.Date;

@Theme("mytheme")
@SuppressWarnings("serial")
public class MyVaadinUI extends UI
{

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = MyVaadinUI.class, widgetset = "com.mycompany.fileman.AppWidgetSet")
    public static class Servlet extends VaadinServlet {
    }

     @Override
    protected void init(VaadinRequest request) {
        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        setContent(layout);
        initAll(layout);
    }

        private TreeTable treetable;

        private void initFileTree(ComponentContainer parentLayout) {
            // Создадим объект TreeTable для отображения иерархических данных в табличном виде
            treetable = new TreeTable("File System");
            treetable.setSelectable(true);
            treetable.setColumnCollapsingAllowed(true);
            treetable.setColumnReorderingAllowed(true);
            treetable.setSizeFull();
            parentLayout.addComponent(treetable);
        // Добавляем обработчик нажатия
        treetable.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                String clickedFilename = itemClickEvent.getItemId().toString(); // Элемент, на котором была нажата кнопка мыши
                System.out.println("ItemClick: pathname:" + clickedFilename);

                // Если двойной клик
                if (itemClickEvent.isDoubleClick()) {
                    doChangeDir(clickedFilename);
                } else {
                    doSelectFile(clickedFilename);
                }
            }
        });
        }
    private void updateFileTree(File sourcePath) {
        // Создаем контейнер файловой системы
        FilesystemContainer currentFileSystem = new FilesystemContainer(sourcePath);
        currentFileSystem.setRecursive(false); // Отключим рекурсивное считывание подкаталогов

        // Связываем его с объектом TreeTable, отображающим файловую систему
        treetable.setContainerDataSource(currentFileSystem);
        treetable.setItemIconPropertyId("Icon");
        treetable.setVisibleColumns(new Object[]{"Name", "Size", "Last Modified"}); // Для того, чтобы скрыть колонку с идентификатором иконки, укажем нужные колонки
    }
        private File currentPath;

    // Вспомогательная функция для получения каталога приложения по умолчанию
    // ~/NetBeansProjects/fileman/target/fileman-1.0-SNAPSHOT/
    private void getDefaultDirectory() {
        UI ui = MyVaadinUI.getCurrent();
        VaadinSession session = ui.getSession();
        VaadinService service = session.getService();
        currentPath = service.getBaseDirectory();
    }
    
    // Инициализация всех элементов
    private void initAll(VerticalLayout layout) {
        initMenuBar(layout);
        initTopPanel(layout);
        initMainPanels(layout);
        getDefaultDirectory();
        updateFileTree(currentPath);                
        initBottomPanel(layout);
    }
        
    // Обновление всех элементов
    private void updateAll() {
        updateFileTree(currentPath);
        updateInfo();
    }
    
    // Обновление информации о файле/каталоге (при изменении файла/каталога)
    private void updateInfo() {
        updateMenuBar();
        updateTopPanel(currentPath, selectedFilename);
        updateBottomPanel(selectedFilename);
        updatePreview(selectedFilename);      
   }
    
        private String selectedFilename;

    // Пользовательское действие - обновление каталога
    private void doRefresh() {
        updateAll();
    }

    // Пользовательское действие - переход в другой каталог
    private void doChangeDir(String path) {
        currentPath = new File(path);
        if (currentPath.isDirectory()) {
            selectedFilename = null;
            updateAll();
        }
    }

    // Пользовательское действие - переход в каталог на уровень выше
    private void doUpLevel() {
        currentPath = currentPath.getParentFile();
        selectedFilename = null;
        updateAll();
    }

    // Пользовательское действие - выбор файла
    private void doSelectFile(String filename) {
        selectedFilename = filename;
        updateInfo();
    }

    private void initMenuBar(Layout parentLayout) {
        // Описание объекта MenuBar
        // https://vaadin.com/book/-/page/components.menubar.html

        // Создаем главное меню 
        MenuBar menuBar = new MenuBar();    // Создаем объект
        menuBar.setWidth("100%");           // Растягиваем на 100% доступной ширины
        parentLayout.addComponent(menuBar); // Добавляем в layout    

        // Добавляем в главное меню подменю "File"
        final MenuItem fileMenuItem = menuBar.addItem("File", null, null);

        // Добавляем в меню "File" элемент "Refresh" и обработчик при его выборе
        fileMenuItem.addItem("Refresh", FontAwesome.REFRESH, new MenuBar.Command() {
            @Override
            public void menuSelected(MenuItem selectedItem) {
                doRefresh();
            }
        });

        // Добавляем в меню "File" элемент  "Up Level" и обработчик при его выборе
        fileMenuItem.addItem("Up Level", FontAwesome.ARROW_UP, new MenuBar.Command() {
            @Override
            public void menuSelected(MenuItem selectedItem) {
                doUpLevel();
            }
        });
    }

    private void updateMenuBar() {
        // Пока ничего не делать
    }

    private Label labelFileName;
 
    // Инициализация верхней панели, содержащей кнопки и текущий путь/выбранный файл
    private void initTopPanel(Layout parentLayout) {
        // Создаем новую горизонтальную компоновку, которая будет служить панелью инструментов
        HorizontalLayout topPanelLayout = new HorizontalLayout();
        topPanelLayout.setWidth("100%");           // Растягиваем на 100% доступной ширины
        topPanelLayout.setSpacing(true);           // Между элементами будет пустое пространство
        parentLayout.addComponent(topPanelLayout); // Добавляем к основной компоновке 

        // Создаем кнопку Refresh
        Button button = new Button("Refresh"); // Создаем объект
        button.setIcon(FontAwesome.REFRESH);   // Задаем иконку из FontAwesome
        // button.addStyleName(ValoTheme.BUTTON_SMALL);      // Есть стили разных размеров
        topPanelLayout.addComponent(button);                 // Добавляем в компоновку
        button.addClickListener(new Button.ClickListener() { // Добавляем обработчик нажатия
            @Override
            public void buttonClick(Button.ClickEvent event) {
                doRefresh();
            }
        });

        // Создаем кнопку Up Level
        button = new Button("Up Level");      // Создаем объект
        button.setIcon(FontAwesome.ARROW_UP); // Задаем иконку из FontAwesome
        // button.addStyleName(ValoTheme.BUTTON_SMALL);      // Есть стили разных размеров
        topPanelLayout.addComponent(button);                 // Добавляем в компоновку
        button.addClickListener(new Button.ClickListener() { // Добавляем обработчик нажатия
            @Override
            public void buttonClick(Button.ClickEvent event) {
                doUpLevel();
            }
        });

        // Добавляем текст с именем выбранного файла
        labelFileName = new Label();                // Создаем объект
        topPanelLayout.addComponent(labelFileName); // Добавляем в компоновку
        topPanelLayout.setComponentAlignment(labelFileName, Alignment.MIDDLE_CENTER);
        topPanelLayout.setExpandRatio(labelFileName, 1); // Данный компонент будет занимать все доступное место
    }

    // Обновление верхней панели
    private void updateTopPanel(File currentPath, String selectedFilename) {
        if (selectedFilename != null) {
            labelFileName.setValue(selectedFilename);
        } else {
            labelFileName.setValue(currentPath.toString());
        }
    }

    Label[] bottomLabels;

    // Инициализация нижней панели, содержащей информацию о выбранном файле
    private void initBottomPanel(Layout parentLayout) {
        final String[] captions = new String[]{
            "File Size (Bytes)", "File Date", "Usable Space (Bytes)", "Total Space (Bytes)", "Free Space (Bytes)"
        };

        HorizontalLayout bottomPanelLayout = new HorizontalLayout();
        bottomPanelLayout.setWidth("100%"); // Растягиваем на 100% доступной ширины
        parentLayout.addComponent(bottomPanelLayout);

        // Создаем объекты Label для отображения информации о файле
        bottomLabels = new Label[captions.length];
        for (int i = 0; i < captions.length; i++) {
            bottomLabels[i] = new Label();
            bottomLabels[i].setCaption(captions[i]);
            bottomLabels[i].setValue("NA");
            bottomPanelLayout.addComponent(bottomLabels[i]);
        }
    }

    // Обновление нижней панели
    private void updateBottomPanel(String pathname) {
        try {
            File file = new File(pathname);
            // Присваиваем значения объектам Label - информация о файле
            bottomLabels[0].setValue(Long.toString(file.length()));
            bottomLabels[1].setValue((new Date(file.lastModified())).toString());
            // Информация о диске
            bottomLabels[2].setValue(Long.toString(file.getUsableSpace()));
            bottomLabels[3].setValue(Long.toString(file.getTotalSpace()));
            bottomLabels[4].setValue(Long.toString(file.getFreeSpace()));
        } catch (Exception e) { // Скроем исключительную ситуацию
            for (Label bottomLabel : bottomLabels) {
                bottomLabel.setValue("NA");
            }
        }
    }
    
        private HorizontalLayout previewLayout;
    private Embedded previewEmbedded;

    // Инициализация основной панели, содержащей просмотр файловой структуры и предварительный просмотр файла
    private void initMainPanels(VerticalLayout parentLayout) {
        HorizontalSplitPanel mainPanels = new HorizontalSplitPanel();
        mainPanels.setSizeFull();
        parentLayout.addComponent(mainPanels);
        parentLayout.setExpandRatio(mainPanels, 1);

        initFileTree(mainPanels);
        initPreview(mainPanels);
    }

    // Инициализация панели предварительного просмотра файла
    private void initPreview(ComponentContainer parentLayout) {
        previewLayout = new HorizontalLayout();
        previewLayout.setSizeFull();
        parentLayout.addComponent(previewLayout);
        
        // Создаем элемент для предпросмотра изображений
        previewEmbedded = new Embedded("Preview area", null); // Создаем объект Embedded
        previewEmbedded.setVisible(true);            // Скрываем элемент
        previewLayout.addComponent(previewEmbedded); // Добавляем в компоновку
        previewLayout.setComponentAlignment(previewEmbedded, Alignment.MIDDLE_CENTER); // Располагаем по центру
    }
    
    // Скрыть предварительный просмотр файла
    private void clearPreview() {
        previewEmbedded.setSource(null);
        previewEmbedded.setVisible(true);
    }

    // Обновить предварительный просмотр файла
    private void updatePreview(String pathname) {
        if (pathname == null || pathname.length() == 0) {
            clearPreview();
            return;
        }

        // Выделим расширение файла
        File file = new File(pathname);
        int lastIndexOf = pathname.lastIndexOf(".");
        String extension = (lastIndexOf == -1) ? "" : pathname.substring(lastIndexOf);

        // Ограничение на размер файла для предпросмотра - до 128КБ
        final int PREVIEW_FILE_LIMIT = 128 * 1024;

        // Расширения файлов для предпросмотра с помощью объекта Embedded (изображения, Flash и т.д.)
        final String[] imageExtensions = new String[]{
            ".gif", ".jpeg", ".jpg", ".png", ".bmp", ".ico", ".cur", "swf", "svg"
        };

        // Скроем оба объекта, используемых для предпросмотра
        previewEmbedded.setVisible(false);

        // Проверим, не превышает ли размер файла пороговый
        if (file.length() > PREVIEW_FILE_LIMIT) {
            clearPreview();
            return;
        }

        // Если расширение файла - в списке изображений
        if (Arrays.asList(imageExtensions).contains(extension)) {
            Resource resource = new FileResource(file); // Создаем файловый ресурс
            previewEmbedded.setSource(resource);        // Задаем источник для объекта Embedded
            previewEmbedded.setVisible(true);           // Показываем объект
            previewLayout.setExpandRatio(previewEmbedded, 1.0f); // Будет занимать все доступное место 
        }
    }    

}
