/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divusi.rptk.bean;

import com.divusi.commons.GrailsRestClient;
import com.divusi.commons.Util;
import static com.divusi.rptk.bean.PengajuanRptkBean.generateRandomString;

import com.divusi.rptk.alfresco.AlfrescoUtil;
import com.divusi.rptk.alfresco.FileAlfresco;
import com.divusi.rptk.model.database.Mom;
import com.divusi.rptk.model.database.PengajuanRptk;
import com.divusi.rptk.model.database.Rptk;
import com.divusi.rptk.model.display.KkksMockup;
import com.divusi.rptk.service.PengajuanService;
import com.divusi.rptk.service.RptkMomService;
import com.divusi.rptk.service.RptkPengajuanService;
import com.divusi.rptk.service.RptkService;
import com.divusi.rptk.util.BaseBeanInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;

import org.omnifaces.util.Faces;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author krisna.fathurahman
 */
@ManagedBean(name = "evaluasiRptk")
@SessionScoped
public class EvaluasiRptkBean implements BaseBeanInterface, Serializable {

    private final String basemodule = "/faces/modules/rptk/evaluasi/";
    private Rptk rptk;
    private List<PengajuanRptk> listPengajuan;
    private List<Mom> listMom;
    private List<Mom> filteredListMom;
    private final RptkPengajuanService rptkPengajuanService = new RptkPengajuanService();
    private String optionsSearch;
    private String searchValue;

    private final RptkService rptkService = new RptkService();
    private int idRptk;
    private int idKkks;
    private String suratPengajuan;

    private String selectedNoPengajuan;
    private String selectedFullPathSuratPernyataan;
    private String selectedFullPathSuratRekomendasiDitjenMigas;
    private String selectedFullPathSuratRekomendasiKemenakertrans;

    private String selectedNameSuratPernyataan;
    private String selectedNameSuratRekomendasiDitjenMigas;
    private String selectedNameSuratRekomendasiKemenakertrans;

    private StreamedContent toDownload;
    private File download;
    private UploadedFile uploadedMom;
    private InputStream is;
    private String ContentType;
    private String optionSearchMom, valueSearchMom;
    private int idPengajuanRptk;
    private Date tanggalSearch;
    private final RptkMomService rptkMomService = new RptkMomService();

    private String orgChartScript = new String();
    private int idHalaman;
    private int idStruktur;

    @Override
    public void search() {
        String paramUrl = "";
        if (searchValue != null && searchValue != "") {
            paramUrl = optionsSearch + "=" + URLEncoder.encode(searchValue);
        }
        listPengajuan = (ArrayList<PengajuanRptk>) this.rptkPengajuanService.search(paramUrl);
        if (listPengajuan.size() > 0) {
            setSuratPengajuan(listPengajuan.get(0).getSuratPengajuan());
        }
    }

    @Override
    public void viewDetail(long id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void viewAll() {
        Util.redirectToPage("/modules/rptk/pengajuan/list_pengajuan.xhtml");
    }

    public void backtoList() {
        Util.redirectToPage("/modules/dashboard/list_pengajuan.xhtml");
    }

    @Override
    public void viewInput() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void viewEdit(long id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void viewDelete(int idx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void viewProses(int idPengajuan) {
        Util.redirectToPage(basemodule + "proses.xhtml?idPengajuan=" + idPengajuan);
    }

    public void viewPosisiTenagaKerja() {
        Util.redirectToPage(basemodule + "proses.xhtml");
    }

    @Override
    @PostConstruct
    public void init() {
        populatePengajuan();
        search();
        filteredListMom = null;
    }

    public void getRptk(long idRptk) {
        RptkPengajuanService rps = new RptkPengajuanService();
        PengajuanRptk pRptk = rps.get(idRptk);
        this.rptk = (Rptk) rptkService.get(pRptk.getIdRptk());
    }

    public void ViewListData() {
        populateMom();
        rptk = new Rptk();
        rptk = (Rptk) rptkService.get(getIdPengajuanRptk());
        setIdRptk((int) rptk.getIdRptk());
        idKkks = (int) rptk.getKkks().getIdKkks();
        rptk.setStLampiran1(rptk.getStLampiran1());
        rptk.setStLampiran2(rptk.getStLampiran2());
        rptk.setStLampiran3(rptk.getStLampiran3());
        rptk.setStLampiran4(rptk.getStLampiran4());
        rptk.setStLampiran5(rptk.getStLampiran5());
        rptk.setStLampiran6(rptk.getStLampiran6());
        rptk.setStLampiran7(rptk.getStLampiran7());
        rptk.setStLampiran8(rptk.getStLampiran8());
    }

    public void populatePengajuan() {
        this.listPengajuan = new ArrayList<PengajuanRptk>();
        ArrayList<PengajuanRptk> listAllEvaluasi = (ArrayList<PengajuanRptk>) rptkPengajuanService.listAll();
        this.listPengajuan = listAllEvaluasi;
    }

    public void populateMom() {
        listMom = (ArrayList<Mom>) rptkMomService.get(this.idPengajuanRptk);

    }

    public InputStream getIs() {
        return is;
    }

    public void setIs(InputStream is) {
        this.is = is;
    }

    public String getContentType() {
        return ContentType;
    }

    public void setContentType(String ContentType) {
        this.ContentType = ContentType;
    }

    public UploadedFile getUploadMom() {
        return uploadedMom;
    }

    public void setUploadFile(UploadedFile uploadedMom) {
        this.uploadedMom = uploadedMom;
    }

    public void setUploadFileEvent(FileUploadEvent event) throws IOException {
        this.uploadedMom = event.getFile();
        this.is = event.getFile().getInputstream();
        this.ContentType = event.getFile().getContentType();
        if (uploadedMom != null) {
            System.out.println("Uploaded Mom Name" + uploadedMom.getFileName());
        }
        String noMom = "";
        if (listMom == null || listMom.size() == 0) {
            noMom = GenerateNoMom("baru");
        } else {
            String mom = listMom.get(listMom.size() - 1).getNoMom();

            noMom = GenerateNoMom(mom);
        }
        DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss");
        Date d = new Date();
        String[] ext = event.getFile().getFileName().split("\\.");
        String fileNewName = "MoM_" + dateFormat.format(d) +"."+ ext[ext.length-1];
        rptkMomService.uploadFile(this.is , this.ContentType, this.idPengajuanRptk, noMom, fileNewName);
        searchMom();

    }

    public String GenerateNoMom(String noTerakhir) {
        String noMom = "MOM";
        String[] temp;
        temp = noTerakhir.split("MOM");
        if (temp.length <= 1) {

            noMom = noMom + "0001";
        } else {
            int tempInt = Integer.parseInt(temp[1]) + 1;
            String result = String.format("%04d", tempInt);
            noMom = noMom + result;
        }

        return noMom;

    }

    public String getOptionSearchMom() {
        return optionSearchMom;
    }

    public void setOptionSearchMom(String optionSearchMom) {
        this.optionSearchMom = optionSearchMom;
    }

    public String getValueSearchMom() {
        return valueSearchMom;
    }

    public void setValueSearchMom(String valueSearchMom) {
        this.valueSearchMom = valueSearchMom;
    }

    public void searchMom() {
        if (tanggalSearch != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            valueSearchMom = sdf.format(tanggalSearch);
            tanggalSearch = null;
        }
        String paramUrl = "idPengajuan=" + this.idPengajuanRptk;
        if (valueSearchMom != null && valueSearchMom != "") {
            paramUrl = paramUrl + "&" + optionSearchMom + "=" + valueSearchMom;
        }
        listMom = (ArrayList<Mom>) rptkMomService.search(paramUrl);

    }

    @Override
    public void save() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Date generateDate(Date create, Date update) {
        if (update != null) {
            return update;
        }
        return create;
    }

    public void handleClickSurat(String paramNoPengajuan, String paramSuratPernyataan, String paramSuratRekomendasiDitjen, String paramSuratRekomendasiKamenaketrans) {
        selectedNoPengajuan = paramNoPengajuan;
        selectedFullPathSuratPernyataan = "-";
        selectedFullPathSuratRekomendasiDitjenMigas = "-";
        selectedFullPathSuratRekomendasiKemenakertrans = "-";
        selectedNameSuratPernyataan = "-";
        selectedNameSuratRekomendasiDitjenMigas = "-";
        selectedNameSuratRekomendasiKemenakertrans = "-";

        if (!paramSuratPernyataan.isEmpty()) {
            String[] splitPathPernyataan = paramSuratPernyataan.split("/");
            selectedNameSuratPernyataan = splitPathPernyataan[splitPathPernyataan.length - 1];
            selectedFullPathSuratPernyataan = paramSuratPernyataan;
        }
        if (!paramSuratRekomendasiDitjen.isEmpty()) {
            String[] splitPathRekomendasi = paramSuratRekomendasiDitjen.split("/");
            selectedNameSuratRekomendasiDitjenMigas = splitPathRekomendasi[splitPathRekomendasi.length - 1];
            selectedFullPathSuratRekomendasiDitjenMigas = paramSuratRekomendasiDitjen;
        }

        if (!paramSuratRekomendasiKamenaketrans.isEmpty()) {
            String[] splitPathRekomendasiKamena = paramSuratRekomendasiKamenaketrans.split("/");
            selectedNameSuratRekomendasiKemenakertrans = splitPathRekomendasiKamena[splitPathRekomendasiKamena.length - 1];
            selectedFullPathSuratRekomendasiKemenakertrans = paramSuratRekomendasiKamenaketrans;
        }

        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('suratDialog').show();");
    }

    public void goToDetail(long idPengajuan){
        Util.redirectToPage(basemodule + "proses.xhtml?idPengajuan="+idPengajuan+"&history=1");
    }
    
    public String identitasKkks(long id) {
        com.divusi.imta.service.RptkService rs = new com.divusi.imta.service.RptkService();
        model.fresh.Rptk rptkTemp = rs.fetchRptkPK((int) id);
        return "/modules/rptk/lampiran/identitas-kkks.xhtml?faces-redirect=true&id=" + rptkTemp.getKkks();
    }

    public String jabatan(long id) {
        com.divusi.imta.service.RptkService rs = new com.divusi.imta.service.RptkService();
        model.fresh.Rptk rptkTemp = rs.fetchRptkPK((int) id);
        return "/modules/rptk/lampiran/jabatan.xhtml?faces-redirect=true&id=" + rptkTemp.getIdRptk();
    }

    public String orgChart() {
        return "/modules/rptk/lampiran/orgchart.xhtml?faces-redirect=true";
    }

    public String penggunaanTka(long id) {
        com.divusi.imta.service.RptkService rs = new com.divusi.imta.service.RptkService();
        model.fresh.Rptk rptkTemp = rs.fetchRptkPK((int) id);
        return "/modules/rptk/lampiran/penggunaan-tka.xhtml?faces-redirect=true&id=" + rptkTemp.getIdRptk();
    }

    public String pelatihan(long id) {
        com.divusi.imta.service.RptkService rs = new com.divusi.imta.service.RptkService();
        model.fresh.Rptk rptkTemp = rs.fetchRptkPK((int) id);
        return "/modules/rptk/lampiran/pelatihan.xhtml?faces-redirect=true&id=" + rptkTemp.getIdRptk();
    }

    public String rekapStruktur(long id) {
        com.divusi.imta.service.RptkService rs = new com.divusi.imta.service.RptkService();
        model.fresh.Rptk rptkTemp = rs.fetchRptkPK((int) id);
        return "/modules/rptk/lampiran/rekap-struktur.xhtml?faces-redirect=true&id=" + rptkTemp.getIdRptk();
    }

    public String programPertukaran(long id) {
        com.divusi.imta.service.RptkService rs = new com.divusi.imta.service.RptkService();
        model.fresh.Rptk rptkTemp = rs.fetchRptkPK((int) id);
        return "/modules/rptk/lampiran/program-pertukaran.xhtml?faces-redirect=true&id=" + rptkTemp.getIdRptk();
    }

    public void downloadFile(String idFile) {
        System.out.println("idFile:" + idFile);
        if (!idFile.isEmpty()) {
            try {
                FileAlfresco fa = new FileAlfresco();
                fa = AlfrescoUtil.getInstance().downloadNew(idFile);
                download = new File(Util.getInitialParam("tmpFolder") + fa.getFilename());
                OutputStream os = new FileOutputStream(download);
                byte[] arr = new byte[1024];
                int i = 0;
                while ((i = fa.getStream().read(arr)) != -1) {
                    os.write(arr, 0, i);
                }
                os.close();
                //proses download file
                System.out.println("==sendToBrowser==");
                Faces.sendFile(download, true);
                download.delete();
                download = null;
            } catch (Exception e) {
                System.out.println("Ada exception");
                e.printStackTrace();
            }
        }
    }

    public List<PengajuanRptk> getListPengajuan() {
        return listPengajuan;
    }

    public void setListPengajuan(List<PengajuanRptk> listPengajuan) {
        this.listPengajuan = listPengajuan;
    }

    public List<Mom> getListMom() {
        return listMom;
    }

    public void setListMom(List<Mom> listMom) {
        this.listMom = listMom;
    }

    public String getOptionsSearch() {
        return optionsSearch;
    }

    public void setOptionsSearch(String optionsSearch) {
        this.optionsSearch = optionsSearch;
    }

    public String getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(String searchValue) {
        this.searchValue = searchValue;
    }

    public String getSelectedNameSuratRekomendasiKemenakertrans() {
        return selectedNameSuratRekomendasiKemenakertrans;
    }

    public void setSelectedNameSuratRekomendasiKemenakertrans(String selectedNameSuratRekomendasiKemenakertrans) {
        this.selectedNameSuratRekomendasiKemenakertrans = selectedNameSuratRekomendasiKemenakertrans;
    }

    public String getSelectedNoPengajuan() {
        return selectedNoPengajuan;
    }

    public void setSelectedNoPengajuan(String selectedNoPengajuan) {
        this.selectedNoPengajuan = selectedNoPengajuan;
    }

    public String getSelectedFullPathSuratPernyataan() {
        return selectedFullPathSuratPernyataan;
    }

    public void setSelectedFullPathSuratPernyataan(String selectedFullPathSuratPernyataan) {
        this.selectedFullPathSuratPernyataan = selectedFullPathSuratPernyataan;
    }

    public String getSelectedFullPathSuratRekomendasiDitjenMigas() {
        return selectedFullPathSuratRekomendasiDitjenMigas;
    }

    public void setSelectedFullPathSuratRekomendasiDitjenMigas(String selectedFullPathSuratRekomendasiDitjenMigas) {
        this.selectedFullPathSuratRekomendasiDitjenMigas = selectedFullPathSuratRekomendasiDitjenMigas;
    }

    public String getSelectedFullPathSuratRekomendasiKemenakertrans() {
        return selectedFullPathSuratRekomendasiKemenakertrans;
    }

    public void setSelectedFullPathSuratRekomendasiKemenakertrans(String selectedFullPathSuratRekomendasiKemenakertrans) {
        this.selectedFullPathSuratRekomendasiKemenakertrans = selectedFullPathSuratRekomendasiKemenakertrans;
    }

    public String getSelectedNameSuratPernyataan() {
        return selectedNameSuratPernyataan;
    }

    public void setSelectedNameSuratPernyataan(String selectedNameSuratPernyataan) {
        this.selectedNameSuratPernyataan = selectedNameSuratPernyataan;
    }

    public String getSelectedNameSuratRekomendasiDitjenMigas() {
        return selectedNameSuratRekomendasiDitjenMigas;
    }

    public void setSelectedNameSuratRekomendasiDitjenMigas(String selectedNameSuratRekomendasiDitjenMigas) {
        this.selectedNameSuratRekomendasiDitjenMigas = selectedNameSuratRekomendasiDitjenMigas;
    }

    public Rptk getRptk() {
        return rptk;
    }

    public void setRptk(Rptk rptk) {
        this.rptk = rptk;
    }

    public int getIdPengajuanRptk() {
        return idPengajuanRptk;
    }

    public void setIdPengajuanRptk(int idPengajuanRptk) {
        this.idPengajuanRptk = idPengajuanRptk;
        ViewListData();
    }

    public Date getTanggalSearch() {
        return tanggalSearch;
    }

    public void setTanggalSearch(Date tanggalSearch) {
        this.tanggalSearch = tanggalSearch;
    }

    public int getIdRptk() {
        return idRptk;
    }

    public void setIdRptk(int idRptk) {
        this.idRptk = idRptk;
    }

    public void lihatLampiran(String idLampiran) {
        //Util.redirectToPage2(idTka);
        int idRptk = this.getIdRptk();
        System.out.println("ID RPTK = " + this.getIdRptk());
        switch (idLampiran) {
            case "1":
                Util.redirectToPage(basemodule + "proses.xhtml?idRptk=" + idRptk);
                break;
            case "2":
                Util.redirectToPage(basemodule + "proses.xhtml?idImta=1");
                break;
            case "3":
                Util.redirectToPage(basemodule + "proses.xhtml?idImta=1");
                break;
            case "4":
                Util.redirectToPage(basemodule + "proses.xhtml?idImta=1");
                break;
            case "5":
                Util.redirectToPage(basemodule + "proses.xhtml?idImta=1");
                break;
            case "6":
                Util.redirectToPage(basemodule + "proses.xhtml?idImta=1");
                break;
            case "7":
                Util.redirectToPage(basemodule + "proses.xhtml?idImta=1");
                break;
            case "8":
                Util.redirectToPage(basemodule + "proses.xhtml?idImta=1");
                break;
            default:

        }

    }

    public int getIdKkks() {
        return idKkks;
    }

    public void setIdKkks(int idKkks) {
        this.idKkks = idKkks;
    }

    public String getSuratPengajuan() {
        return suratPengajuan;
    }

    public void setSuratPengajuan(String suratPengajuan) {
        this.suratPengajuan = suratPengajuan;
    }

    public void deleteFile(int idFile) {
        rptkMomService.delete(idFile);
        populateMom();

    }

    public List<Mom> getFilteredListMom() {
        return filteredListMom;
    }

    public void setFilteredListMom(List<Mom> filteredListMom) {
        this.filteredListMom = filteredListMom;
    }

    public String formatedDate(Date date) {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        if (date != null) {
            return df.format(date);
        } else {
            return "";
        }
    }

    public String getOrgChartScript() {
        this.orgChartScript = new String();
        this.orgChartScript = "<script type=\"text/javascript\">\n"
                + "            // GRAPH & IT'S GRID\n"
                + "            var graph = new joint.dia.Graph;\n"
                + "\n"
                + "            var grid = 132;\n"
                + "\n"
                + "            var paper = new joint.dia.Paper({\n"
                + "                el: $('#paper'),\n"
                + "                width: 1056,\n"
                + "                height: 924,\n"
                + "                model: graph,\n"
                + "                gridSize: 1\n"
                + "            });\n"
                + "\n"
                + "            function setGrid(paper, gridSize, color) {\n"
                + "                // Set grid size on the JointJS paper object (joint.dia.Paper instance)\n"
                + "                paper.options.gridSize = gridSize / 6;\n"
                + "                // Draw a grid into the HTML 5 canvas and convert it to a data URI image\n"
                + "                var canvas = $('<canvas/>', {width: gridSize, height: gridSize});\n"
                + "                canvas[0].width = gridSize;\n"
                + "                canvas[0].height = gridSize;\n"
                + "                var context = canvas[0].getContext('2d');\n"
                + "                context.beginPath();\n"
                + "                context.moveTo(0, 0);\n"
                + "                context.lineTo(gridSize, 0);\n"
                + "                context.moveTo(0, 0);\n"
                + "                context.lineTo(0, gridSize);\n"
                + "                context.strokeStyle = '#bbbbbb';\n"
                + "                context.stroke();\n"
                + "                // Finally, set the grid background image of the paper container element.\n"
                + "                var gridBackgroundImage = canvas[0].toDataURL('image/png');\n"
                + "                paper.$el.css('background-image', 'url(\"' + gridBackgroundImage + '\")');\n"
                + "            }\n"
                + "\n"
                + "            setGrid(paper, grid, '#000000');\n"
                + "\n"
                + "            // CUSTOM ELEMENT\n"
                + "            var Icon = joint.shapes;\n"
                + "            Icon.html = {};\n"
                + "            Icon.html.Element = joint.shapes.basic.Rect.extend({\n"
                + "                defaults: joint.util.deepSupplement({\n"
                + "                    type: 'html.Element',\n"
                + "                    attrs: {\n"
                + "                        rect: {stroke: 'none', 'fill-opacity': 0}\n"
                + "                    }\n"
                + "                }, joint.shapes.basic.Rect.prototype.defaults)\n"
                + "            });\n"
                + "            Icon.html.ElementView = joint.dia.ElementView.extend({\n"
                + "                template: [\n"
                + "                    '<div class=\"icon-element\">',\n"
                + "                    '<div class=\"border\">',\n"
                + "                    '<p class=\"nama-halaman\"></p>',\n"
                + "                    '<br/>',\n"
                + "                    '<p class=\"no-halaman\"></p>',\n"
                + "                    '<p class=\"nama-jabatan\"></p>',\n"
                + "                    '<hr/>',\n"
                + "                    '<ul class=\"pengisi-list\"></ul>',\n"
                + "                    '<div class=\"koordinasi\">',\n"
                + "                    '<hr/>',\n"
                + "                    '<p class=\"koordinasi-label\">Jabatan koordinasi:</p><br/>',\n"
                + "                    '<ul class=\"koordinasi-list\"></ul>',\n"
                + "                    '</div>',\n"
                + "                    '</div>',\n"
                + "                    '</div>'\n"
                + "                ].join(''),\n"
                + "                initialize: function () {\n"
                + "                    _.bindAll(this, 'updateBox');\n"
                + "                    joint.dia.ElementView.prototype.initialize.apply(this, arguments);\n"
                + "                    this.$box = $(_.template(this.template)());\n"
                + "                    // Update the box position whenever the underlying model changes.\n"
                + "                    this.model.on('change', this.updateBox, this);\n"
                + "                    // Remove the box when the model gets removed from the graph.\n"
                + "                    this.model.on('remove', this.removeBox, this);\n"
                + "                    this.updateBox();\n"
                + "                },\n"
                + "                render: function () {\n"
                + "                    joint.dia.ElementView.prototype.render.apply(this, arguments);\n"
                + "                    this.paper.$el.prepend(this.$box);\n"
                + "                    this.updateBox();\n"
                + "                    return this;\n"
                + "                },\n"
                + "                updateBox: function () {\n"
                + "                    // Set the position and dimension of the box so that it covers the JointJS element.\n"
                + "                    var bbox = this.model.getBBox();\n"
                + "                    // Example of updating the HTML with a data stored in the cell model.\n"
                + "                    if (this.model.get('tipe') == \"Jabatan\") {\n"
                + "                        this.$box.find('.nama-halaman').hide();\n"
                + "                        this.$box.find('br').hide();\n"
                + "                        this.$box.find('.no-halaman').hide();\n"
                + "                        this.$box.find('.nama-jabatan').text(this.model.get('namaJabatan'));\n"
                + "\n"
                + "                        var pengisi = \"\";\n"
                + "                        if (this.model.get('listPengisi') != null && this.model.get('listPengisi').length > 0) {\n"
                + "                            var listPengisi = this.model.get('listPengisi');\n"
                + "                            for (var i = 0; i < listPengisi.length; i++) {\n"
                + "                                if (listPengisi[i].includes(\"(EE)\")) {\n"
                + "                                    pengisi += '<li class=\"pengisi-item pengisi-asing\">' + listPengisi[i] + '</li>';\n"
                + "                                } else {\n"
                + "                                    pengisi += '<li class=\"pengisi-item\">' + listPengisi[i] + '</li>';\n"
                + "                                }\n"
                + "                            }\n"
                + "                        }\n"
                + "                        this.$box.find('.pengisi-list').html(pengisi);\n"
                + "\n"
                + "                        var koordinasi = \"\";\n"
                + "                        if (this.model.get('listKoordinasi') == null || this.model.get('listKoordinasi').length < 1) {\n"
                + "                            this.$box.find('.koordinasi').hide();\n"
                + "                        } else {\n"
                + "                            var listKoordinasi = this.model.get('listKoordinasi');\n"
                + "                            for (var i = 0; i < listKoordinasi.length; i++) {\n"
                + "                                koordinasi += '<li class=\"koordinasi-item\">' + listKoordinasi[i] + '</li>';\n"
                + "                            }\n"
                + "                        }\n"
                + "                        this.$box.find('.koordinasi-list').html(koordinasi);\n"
                + "                    } else if (this.model.get('tipe') == \"Halaman\") {\n"
                + "                        this.$box.find('.border').css('border', 'none');\n"
                + "                        this.$box.find('.nama-jabatan').hide();\n"
                + "                        this.$box.find('hr').hide();\n"
                + "                        this.$box.find('.pengisi-list').hide();\n"
                + "                        this.$box.find('.koordinasi').hide();\n"
                + "                        this.$box.find('.nama-halaman').text(this.model.get('namaHalaman'));\n"
                + "                        this.$box.find('.no-halaman').text(\"Halaman \" + this.model.get('noHalaman'));\n"
                + "                    } else {\n"
                + "                        this.$box.hide();\n"
                + "                    }\n"
                + "\n"
                + "\n"
                + "                    this.$box.css({width: bbox.width - 10, left: bbox.x, top: bbox.y, transform: 'rotate(' + (this.model.get('angle') || 0) + 'deg)'});\n"
                + "                },\n"
                + "                removeBox: function (evt) {\n"
                + "                    this.$box.remove();\n"
                + "                }\n"
                + "            });\n"
                + "\n"
                + "            // CONNECTOR LINK\n"
                + "            function link(source, target) {\n"
                + "                var cell = new joint.shapes.org.Arrow({\n"
                + "                    source: {id: source.id},\n"
                + "                    target: {id: target.id},\n"
                + "                    vertices: [\n"
                + "                        {x: source.position().x + Math.round(grid / 3), y: source.position().y + grid + Math.round(grid / 2)},\n"
                + "                        {x: target.position().x + Math.round(grid / 3), y: source.position().y + grid + Math.round(grid / 2)}\n"
                + "                    ],\n"
                + "                    attrs: {\n"
                + "                        '.connection': {\n"
                + "                            'fill': 'none',\n"
                + "                            'stroke-linejoin': 'round',\n"
                + "                            'stroke-width': '2',\n"
                + "                            'stroke': '#4b4a67'\n"
                + "                        },\n"
                + "                        '.marker-vertices': {display: 'none'},\n"
                + "                        '.marker-arrowheads': {display: 'none'},\n"
                + "                        '.connection-wrap': {display: 'none'},\n"
                + "                        '.link-tools': {display: 'none'}\n"
                + "                    }\n"
                + "                });\n"
                + "                graph.addCell(cell);\n"
                + "                return cell;\n"
                + "            }\n"
                + "\n"
                + "            // DRAG EVENT\n"
                + "            paper.on('cell:pointerup',\n"
                + "                    function (cellView, evt, x, y) {\n"
                + "                        // Stick elements into grid\n"
                + "                        var x = cellView.model.get('position').x;\n"
                + "                        var y = cellView.model.get('position').y;\n"
                + "                        var newX = Math.round(x / grid) * grid;\n"
                + "                        var newY = Math.round(y / grid) * grid;\n"
                + "                        if (newX < 0) {\n"
                + "                            newX = newX + grid;\n"
                + "                        }\n"
                + "                        if (newY < 0) {\n"
                + "                            newY = newY + grid;\n"
                + "                        }\n"
                + "                        // Update connector link\n"
                + "                        var connector = cellView.model.get('connector');\n"
                + "                        connector.set('position', {x: newX + Math.round(grid / 6), y: newY + Math.round(grid / 9)});\n"
                + "                        cellView.model.set('connector', connector);\n"
                + "                        cellView.model.set('position', {x: newX, y: newY});\n"
                + "                        var ls = graph.getConnectedLinks(connector);\n"
                + "                        for (i = 0; i < ls.length; i++) {\n"
                + "                            var l = ls[i];\n"
                + "                            var s = l.getSourceElement();\n"
                + "                            var t = l.getTargetElement();\n"
                + "                            l.remove();\n"
                + "                            link(s, t);\n"
                + "                        }\n"
                + "                    }\n"
                + "            );\n"
                + "\n";
                
                this.orgChartScript += "            // POPULATE ELEMENTS\n"
                + "            var arr = [];\n"
                + "\n"
                + "            var el4 = new Icon.html.Element({position: {x: (grid * 3) + Math.round(grid / 6), y: Math.round(grid / 9)}, size: {width: grid - Math.round(grid / 3), height: grid - Math.round(grid / 2)}});\n"
                + "            graph.addCell(el4);\n"
                + "            var el5 = new Icon.html.Element({position: {x: (grid * 2) + Math.round(grid / 6), y: (grid * 2) + Math.round(grid / 9)}, size: {width: grid - Math.round(grid / 3), height: grid - Math.round(grid / 2)}});\n"
                + "            graph.addCell(el5);\n"
                + "            var el6 = new Icon.html.Element({position: {x: (grid * 3) + Math.round(grid / 6), y: (grid * 5) + Math.round(grid / 9)}, size: {width: grid - Math.round(grid / 3), height: grid - Math.round(grid / 2)}});\n"
                + "            graph.addCell(el6);\n"
                + "            var el7 = new Icon.html.Element({position: {x: (grid * 5) + Math.round(grid / 6), y: (grid * 2) + Math.round(grid / 9)}, size: {width: grid - Math.round(grid / 3), height: grid - Math.round(grid / 2)}});\n"
                + "            graph.addCell(el7);\n"
                + "\n"
                + "            var el1 = new Icon.html.Element({\n"
                + "                position: {x: grid * 3, y: 0},\n"
                + "                size: {width: grid, height: grid},\n"
                + "                tipe: 'Jabatan',\n"
                + "                namaJabatan: 'DRILLING MANAGER',\n"
                + "                listPengisi: [\"VACANT (EN)\"],\n"
                + "                connector: el4\n"
                + "            });\n"
                + "            var el2 = new Icon.html.Element({\n"
                + "                position: {x: grid * 2, y: grid * 2},\n"
                + "                size: {width: grid, height: grid},\n"
                + "                tipe: 'Jabatan',\n"
                + "                namaJabatan: 'DRILLING ENGINEER',\n"
                + "                listPengisi: [\"Mara Kudadiri (EN)\"],\n"
                + "                listKoordinasi: [\"OPERATIONAL MANAGER\"],\n"
                + "                connector: el5\n"
                + "            });\n"
                + "            var el3 = new Icon.html.Element({\n"
                + "                position: {x: grid * 3, y: grid * 5},\n"
                + "                size: {width: grid, height: grid},\n"
                + "                tipe: 'Halaman',\n"
                + "                namaHalaman: 'OPERATIONAL',\n"
                + "                noHalaman: '2',\n"
                + "                connector: el6\n"
                + "            });\n"
                + "            var el8 = new Icon.html.Element({\n"
                + "                position: {x: grid * 5, y: grid * 2},\n"
                + "                size: {width: grid, height: grid},\n"
                + "                tipe: 'Jabatan',\n"
                + "                namaJabatan: 'DRILLING STAFF',\n"
                + "                listPengisi: [\"VACANT (EE)\", \"VACANT (EE)\"],\n"
                + "                connector: el7\n"
                + "            });\n"
                + "\n"
                + "            arr.push(el1);\n"
                + "            arr.push(el2);\n"
                + "            arr.push(el3);\n"
                + "            arr.push(el8);\n"
                + "            graph.addCells(arr);\n"
                + "            link(el4, el5);\n"
                + "            link(el5, el6);\n"
                + "            link(el4, el7);\n"
                + "\n"
                + "            // paper.on('cell:pointerclick', \n"
                + "            //     function(cellView, evt, x, y) { \n"
                + "            //         alert('cell view ' + cellView.model.id + ' was clicked'); \n"
                + "            //     }\n"
                + "            // );\n"
                + "\n";
                
                this.orgChartScript += "        </script>";
        return this.orgChartScript;
    }

    public int getIdHalaman() {
        return idHalaman;
    }

    public void setIdHalaman(int idHalaman) {
        this.idHalaman = idHalaman;
    }

    public int getIdStruktur() {
        return idStruktur;
    }

    public void setIdStruktur(int idStruktur) {
        this.idStruktur = idStruktur;
    }

}
