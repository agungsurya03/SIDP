/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divusi.rptk.bean;

import com.divusi.commons.NotificationMailer;
import java.io.File;
import java.io.FileOutputStream;

import java.io.OutputStream;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import org.primefaces.model.StreamedContent;
import com.divusi.commons.Util;
import com.divusi.imta.bean.TambahPengajuanBean;
import com.divusi.imta.model.database.ImtaPengajuan;
import com.divusi.imta.model.display.ViewDokumen;
import com.divusi.rptk.model.display.ViewSurat;
import com.divusi.rptk.model.UploadResponse;
import com.divusi.rptk.alfresco.AlfrescoUtil;
import com.divusi.rptk.alfresco.FileAlfresco;
import com.divusi.rptk.auth.AuthBean;
import com.divusi.rptk.model.database.Kkks;
import com.divusi.rptk.model.database.PengajuanRptk;

import com.divusi.rptk.model.display.PosisiPengajuanMockup;
import com.divusi.rptk.model.display.RiwayatPengajuanImta;
import com.divusi.rptk.model.database.RUser;
import com.divusi.rptk.model.database.Rptk;
import com.divusi.rptk.model.database.RptkKomentar;
import com.divusi.rptk.model.database.RptkSurat;
import com.divusi.rptk.model.database.RptkTemplate;
import com.divusi.rptk.service.EmailService;
import com.divusi.rptk.service.RptkKomentarService;
import com.divusi.rptk.service.RptkPengajuanService;
import com.divusi.rptk.service.RptkPosisiService;
import com.divusi.rptk.service.RptkService;
import com.divusi.rptk.service.RptkSuratService;
import com.divusi.rptk.service.RptkTemplateService;
import com.divusi.rptk.util.BaseBeanInterface;
import com.divusi.rptk.util.RptkManipulator;
import com.divusi.skkmigas.constant.Action;
import com.divusi.skkmigas.constant.Position;
import com.divusi.skkmigas.constant.Status;
import com.divusi.skkmigas.constant.TableType;
import com.divusi.skkmigas.domain.WorkflowResponse;
import com.divusi.skkmigas.workflow.RptkWorkflow;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import static com.sun.jersey.core.util.ReaderWriter.BUFFER_SIZE;
import java.io.IOException;
import java.io.InputStream;

import java.io.Serializable;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.SessionScoped;
import org.omnifaces.util.Faces;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
//import org.primefaces.model.StreamedContent;
//import org.primefaces.model.UploadedFile;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import net.sf.jasperreports.engine.util.JsonUtil;
import org.apache.commons.io.FilenameUtils;

import org.primefaces.model.UploadedFile;

/**
 *
 * @author krisna.fathurahman
 * @modificated by Jauhari Khairul Kawistara
 */
@ManagedBean(name = "pengajuanRPTK")
@ViewScoped
public class PengajuanRptkBean implements BaseBeanInterface, Serializable {

    FileAlfresco fa = new FileAlfresco();
    private List<PengajuanRptk> models = new ArrayList<>();
    private List<PengajuanRptk> modelAmbilTiket = new ArrayList<>();
    private List<PengajuanRptk> modelRiwayat = new ArrayList<>();
    private List<PengajuanRptk> filteredModelRiwayat = new ArrayList<>();
    private List<PengajuanRptk> listPengajuanDalamProses;
    private List<PengajuanRptk> listAmbilTiket;
    private List<PengajuanRptk> listPengajuanDiajukan;
    private List<String> listJenis;
    private List<String> listKuartal;
    private List<String> listKkks;
    private List<PosisiPengajuanMockup> posisiPengajuanList = new ArrayList<>();
    private int id_pengajuan;
    private int editmode;
    private final RptkPengajuanService pengajuanService = new RptkPengajuanService();
    private final String basemodule = "/faces/modules/rptk/pengajuan/";
    private final RptkKomentarService rptkKomentarService = new RptkKomentarService();
    private final RptkService rptkService = new RptkService();
    private PosisiPengajuanMockup posisiPengajuanModel = new PosisiPengajuanMockup();
    private PengajuanRptk pengajuanRptkModel = new PengajuanRptk();
    private PengajuanRptk revisiRptkModel = new PengajuanRptk();
    private PengajuanRptk perbaikanRptkModel = new PengajuanRptk();
    private String searchForm;
    private String optionSearch;
    private String selectedNoPengajuan;
    private String selectedFullPathSuratPersetujuan;
    private String selectedFullPathSuratRekomendasi;
    private String selectedNameSuratPersetujuan;
    private String selectedNameSuratRekomendasi;
    private ArrayList<RptkKomentar> listHistoryKomentar = new ArrayList<>();
    private String historyKomentar;
    @ManagedProperty(value = "#{auth}")
    private AuthBean authBean;

    private int preId;
    private String tahunPeriodeAwal;
    private String periodeAkhirKuartal;
    private String tahunPeriodeAkhir;
    private String periodeAwalKuartal;

    private ViewSurat viewSurat = new ViewSurat();
    private UploadedFile currentfile;
    private StreamedContent toDownload;
    private File download;
    private String noPengajuanBaru;
    private String komentar;
    private RptkWorkflow rwf;
    public String eventPopUpDisplay;
    private String renameFile;
    private RUser user = new RUser();

    private String noRevisiBaru;

    private RptkSurat rptkSurat;
    private RptkSuratService rptkSuratService = new RptkSuratService();
    private final RptkTemplateService rptkTemplateService = new RptkTemplateService();
    private RptkPosisiService rptkPosisiService = new RptkPosisiService();
    private ArrayList<RptkSurat> rptkSuratList = new ArrayList<>();
    private String curentRole;
    private String type = "Baru";

    private final EmailService email = new EmailService();

    public List<PengajuanRptk> getListAmbilTiket() {
        return listAmbilTiket;
    }

    public void setListAmbilTiket(List<PengajuanRptk> listAmbilTiket) {
        this.listAmbilTiket = listAmbilTiket;
    }

    public void unduh(String idFile) {
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
                // proses download file
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

    public String getSelectedFullPathSuratPersetujuan() {
        return selectedFullPathSuratPersetujuan;
    }

    public void setSelectedFullPathSuratPersetujuan(String selectedFullPathSuratPersetujuan) {
        this.selectedFullPathSuratPersetujuan = selectedFullPathSuratPersetujuan;
    }

    public String getSelectedFullPathSuratRekomendasi() {
        return selectedFullPathSuratRekomendasi;
    }

    public void setSelectedFullPathSuratRekomendasi(String selectedFullPathSuratRekomendasi) {
        this.selectedFullPathSuratRekomendasi = selectedFullPathSuratRekomendasi;
    }

    public String getSelectedNameSuratPersetujuan() {
        return selectedNameSuratPersetujuan;
    }

    public void setSelectedNameSuratPersetujuan(String selectedNameSuratPersetujuan) {
        this.selectedNameSuratPersetujuan = selectedNameSuratPersetujuan;
    }

    public String getSelectedNameSuratRekomendasi() {
        return selectedNameSuratRekomendasi;
    }

    public void setSelectedNameSuratRekomendasi(String selectedNameSuratRekomendasi) {
        this.selectedNameSuratRekomendasi = selectedNameSuratRekomendasi;
    }

    public void handleClickSurat(String paramNoPengajuan, String paramSuratPersetujuan, String paramSuratRekomendasi) {
        selectedNoPengajuan = paramNoPengajuan;
        selectedFullPathSuratPersetujuan = "-";
        selectedFullPathSuratRekomendasi = "-";
        selectedNameSuratPersetujuan = "-";
        selectedNameSuratRekomendasi = "-";

        if (!paramSuratPersetujuan.isEmpty()) {
            String[] splitPathPersetujuan = paramSuratPersetujuan.split("/");
            selectedNameSuratPersetujuan = splitPathPersetujuan[splitPathPersetujuan.length - 1];
            selectedFullPathSuratPersetujuan = paramSuratPersetujuan;
        }
        if (!paramSuratRekomendasi.isEmpty()) {
            String[] splitPathRekomendasi = paramSuratRekomendasi.split("/");
            selectedNameSuratRekomendasi = splitPathRekomendasi[splitPathRekomendasi.length - 1];
            selectedFullPathSuratRekomendasi = paramSuratRekomendasi;
        }

        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('downloadDialog').show();");
    }

    public int getId_pengajuan() {
        return id_pengajuan;
    }

    public void setId_pengajuan(int id_pengajuan) {
        this.id_pengajuan = id_pengajuan;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void search() {
        long idKkks = authBean.getSession().getIdkkks();
        String paramUrl = optionSearch + "=" + URLEncoder.encode(searchForm) + "&idKkks=" + idKkks;
        System.out.println(paramUrl);
        modelRiwayat = (ArrayList<PengajuanRptk>) pengajuanService.search(paramUrl);
    }

    @Override
    public void viewDetail(long id) {

        throw new UnsupportedOperationException("Not supported yet."); // To
        // change
        // body
        // of
        // generated
        // methods,
        // choose
        // Tools
        // |
        // Templates.
    }

    @Override
    public void viewAll() {
        Util.redirectToPage(basemodule + "list_pengajuan.xhtml");
    }

    @Override
    public void viewInput() {
        Util.redirectToPage(basemodule + "add.xhtml");
    }

    public void viewPosisi(int idRptk) {
        Util.redirectToPage(basemodule + "posisi_pengajuan.xhtml?idRptk=" + idRptk);
    }

    public void viewUploadSurat(String noPengajuan) {
        Util.redirectToPage(basemodule + "upload_surat.xhtml?noPengajuan=" + noPengajuan);
    }

    public void viewPerbaikan(int idPengajuan) {
        Util.redirectToPage(basemodule + "perbaikan.xhtml?id_pengajuan=" + idPengajuan);
    }

    public void viewRevisi(int idPengajuan) {
        Util.redirectToPage(basemodule + "revisi.xhtml?id_pengajuan=" + idPengajuan);
    }

    @Override
    public void viewEdit(long id) {
        throw new UnsupportedOperationException("Not supported yet."); // To
        // change
        // body
        // of
        // generated
        // methods,
        // choose
        // Tools
        // |
        // Templates.
    }

    @Override
    public void viewDelete(int idx) {
        throw new UnsupportedOperationException("Not supported yet."); // To
        // change
        // body
        // of
        // generated
        // methods,
        // choose
        // Tools
        // |
        // Templates.
    }

    @Override
    @PostConstruct
    public void init() {
        populatePengajuan();
        filteredModelRiwayat = null;
        user = this.GetKkks();
        listJenis = new ArrayList<>();
        listJenis.add("Eksplorasi");
        listJenis.add("Produksi");
        listKkks = new ArrayList<>();
        if (user != null) {
            listKkks.add(user.getName());
            pengajuanRptkModel.setKkks(user.getName());
        }
        listKuartal = new ArrayList<>();
        listKuartal.add("1");
        listKuartal.add("2");
        listKuartal.add("3");
        listKuartal.add("4");

        pengajuanRptkModel.setJenisKkks("Eksplorasi");
        if (authBean.getSession().getIdkkks() != null) {
            Rptk rptk = rptkService.latestRptkByKkksId(authBean.getSession().getIdkkks());
            if (rptk != null) {
                System.out.println("Apakah ke load engga y: ");
                rptk.setKkks(null);
                rptk.setId(rptk.getIdRptk());
                pengajuanRptkModel.setRptk(rptk);
                type = pengajuanService.isPerpanjangan(rptk.getIdRptk());
            } else {
                Util.redirectToPage("/faces/modules/welcome/index.xhtml?e=rptk");
            }
        }
        pengajuanRptkModel.setJenisKkks("Eksplorasi");
    }

    public void checkTahunAwal() {
        if (authBean.getSession().getIdkkks() != null) {
            Rptk rptk = rptkService.latestRptkByKkksId(authBean.getSession().getIdkkks());
            if (rptk != null) {
                if (pengajuanService.getTahunAwal(authBean.getSession().getIdkkks(), rptk.getIdRptk())) {
                    Util.redirectToPage("/faces/modules/welcome/index.xhtml?e=available");
                }
            } else {
                Util.redirectToPage("/faces/modules/welcome/index.xhtml?e=rptk");
            }
        }
    }

    // method dummy posisi pengajuan RPTK
    public void populatePosisiDummy() {
        posisiPengajuanModel = new PosisiPengajuanMockup("noPosisi 1", "namaPosisi1", "department1", "pengisi1",
                "durasi1", "setuju", "keterangan1");
        posisiPengajuanList.add(posisiPengajuanModel);
        posisiPengajuanModel = new PosisiPengajuanMockup("noPosisi 2", "namaPosisi2", "department2", "pengisi2",
                "durasi2", "belum", "keterangan1");
        posisiPengajuanList.add(posisiPengajuanModel);
        posisiPengajuanModel = new PosisiPengajuanMockup("noPosisi 3", "namaPosisi3", "department3", "pengisi3",
                "durasi3", "tolak", "keterangan1");
        posisiPengajuanList.add(posisiPengajuanModel);
        posisiPengajuanModel = new PosisiPengajuanMockup("noPosisi 4", "namaPosisi4", "department4", "pengisi4",
                "durasi4", "belum", "keterangan1");
        posisiPengajuanList.add(posisiPengajuanModel);
    }

    // public void populatePengajuan() {
    // populatePosisiDummy();
    // models = (ArrayList<PengajuanRptk>) pengajuanService.listAll();
    //
    // String usernameSession = authBean.getSession().getUsername();
    // String rolesSession = authBean.oleh();
    //
    // // Workflow Additional
    // if (wm == null)
    // wm = new WorkflowManager(Util.getInitialParam("workflowServerUrl"),
    // Util.getInitialParam("workflowServerQuery"),
    // "ServerTaskImplementationService");
    public void populatePengajuan() {
        //populatePosisiDummy();
        // models = (ArrayList<PengajuanRptk>) pengajuanService.listAll();

        String usernameSession = authBean.getSession().getUsername();
        String rolesSession = authBean.olehRole();
        System.out.println("rolesSession= " + rolesSession);
        System.out.println("usernameSession= " + usernameSession);
        List<Long> listIdPengajuanEvaluasi = new ArrayList<Long>();
        HashMap<String, String> mapActiveButtonEvaluasi = new HashMap<String, String>();
        try {
            if (rwf == null) {
                rwf = new RptkWorkflow(Util.getInitialParam("workflowServerUrl"),
                        Util.getInitialParam("workflowServerQuery"),
                        Util.getInitialParam("workflowServerTaskImplementation"));
            }
            if (usernameSession != null) {
                WorkflowResponse tiketResponse = rwf.searchTaskPrivate(usernameSession, rolesSession, TableType.Ambil_Tiket);
                System.out.println("list id response: " + tiketResponse.getListId().toString());
                if (tiketResponse != null) {
                    List<PengajuanRptk> listTiketResponse = (List<PengajuanRptk>) pengajuanService
                            .getUsingLong(tiketResponse.getListId());
                    this.modelAmbilTiket = listTiketResponse;
                }

                WorkflowResponse riwayat = rwf.searchTaskPrivate(usernameSession, rolesSession, TableType.Riwayat);
                if (riwayat != null && authBean.getSession().getIdkkks() != null) {
                    Long kkksIdSession = authBean.getSession().getIdkkks();
                    System.out.println("KKKS ID " + kkksIdSession);
                    List<PengajuanRptk> listRiwayat = (List<PengajuanRptk>) pengajuanService.getUsingLong(riwayat.getListId());
                    List<PengajuanRptk> listRiwayatKkks = (List<PengajuanRptk>) pengajuanService.getPengajuanByKkksId(kkksIdSession);
                    for (int i = 0; i < listRiwayat.size(); i++) {
                        for (int j = 0; j < listRiwayatKkks.size(); j++) {
                            if (listRiwayat.get(i).getIdPengajuan() == listRiwayatKkks.get(j).getIdPengajuan()) {
                                listRiwayatKkks.remove(j);
                                j--;
                            } else {
                                listRiwayatKkks.get(j).setStatus("Closed");
                            }
                        }
                    }
                    Boolean mergeHistory = listRiwayat.removeAll(listRiwayatKkks);
                    this.modelRiwayat = listRiwayat;
                    this.modelRiwayat.addAll(listRiwayatKkks);
                    for (int i = 0; i < modelRiwayat.size(); i++) {
                        if (modelRiwayat.get(i).getRptk().getId_rptk() < 1) {
                            PengajuanRptk temp = pengajuanService.getById(modelRiwayat.get(i).getIdPengajuan());
                            modelRiwayat.get(i).getRptk().setId_rptk(temp.getRptk().getId_rptk());
                        }
                    }
                    Collections.sort(this.modelRiwayat, new Comparator<PengajuanRptk>() {
                        @Override
                        public int compare(final PengajuanRptk object1, final PengajuanRptk object2) {
                            return object2.getUpdatedDate().compareTo(object1.getUpdatedDate());
                        }
                    });
                }

                WorkflowResponse evaluasiResponse = rwf.searchTaskPrivate(usernameSession, rolesSession, TableType.Evaluasi);
                
                System.out.println("evaluasiResponse: " + evaluasiResponse.toString());
                
                if ((evaluasiResponse.getListId() != null) && (evaluasiResponse.getListId().size() != 0)) {
                    List<PengajuanRptk> listPengajuanEvaluasi = (List<PengajuanRptk>) pengajuanService
                            .getUsingLong(evaluasiResponse.getListId());
                    
                    //System.out.println("listPengajuanEvaluasi: " + listPengajuanEvaluasi.size());

                    if ((listPengajuanEvaluasi != null) && (listPengajuanEvaluasi.size() != 0)) {
                        
                        //System.out.println("masuk ifs.." + listPengajuanEvaluasi.toString());

                        for (PengajuanRptk pengajuanRptk : listPengajuanEvaluasi) {
                            
                            //System.out.println("masuk for.. pengajuan id: " + pengajuanRptk.getIdPengajuan());
                            
                            //System.out.println("setuju button: " + evaluasiResponse.getMapActiveButtonById(pengajuanRptk.getIdPengajuan()).toString());
                            
                            // pengajuanRptk.setActiveButtons(evaluasiResponse.getMapActiveButtonById(pengajuanRptk.getIdRptk()));
                            pengajuanRptk.setSetujuButton(
                                    evaluasiResponse.getMapActiveButtonById(pengajuanRptk.getIdPengajuan()).get(Action.Setuju));
                            //System.out.println("oke setuju button");
                            pengajuanRptk.setKembalikanKeKKKSButton(evaluasiResponse
                                    .getMapActiveButtonById(pengajuanRptk.getIdPengajuan()).get(Action.Kembali_Ke_Kkks));
                            //System.out.println("oke kembalikan kkks button");
                            pengajuanRptk.setKembalikanKeStafButton(evaluasiResponse
                                    .getMapActiveButtonById(pengajuanRptk.getIdPengajuan()).get(Action.Kembali_Ke_Staff));
                            //System.out.println("oke kekmbali staf button");
                            pengajuanRptk.setSelesaiFinalButton(evaluasiResponse
                                    .getMapActiveButtonById(pengajuanRptk.getIdPengajuan()).get(Action.Selesai_Final));
                            //System.out.println("oke selesai final button");
                            pengajuanRptk.setSelesaiSebagianButton(evaluasiResponse
                                    .getMapActiveButtonById(pengajuanRptk.getIdPengajuan()).get(Action.Selesai_Sebagian));
                            //System.out.println("oke selesai sebagian button");

                            System.out.println("1a = " + pengajuanRptk.getIdPengajuan());
                            System.out.println("2a = " + pengajuanRptk.getSetujuButton());
                            System.out.println("3a = " + pengajuanRptk.getKembalikanKeKKKSButton());
                            System.out.println("4a = " + pengajuanRptk.getKembalikanKeStafButton());
                            System.out.println("5a = " + pengajuanRptk.getSelesaiFinalButton());
                            System.out.println("6a = " + pengajuanRptk.getSelesaiSebagianButton());

                        }

                    }

                    this.models = listPengajuanEvaluasi;
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

    }

    @Override
    public void save() {
        System.out.println("start di execute");
        pengajuanRptkModel.setKkksReference(new Kkks());
        pengajuanRptkModel.getKkksReference().setIdKkks(authBean.getSession().getIdkkks());
        pengajuanRptkModel.setType(type);
        pengajuanRptkModel.setPeriode(pengajuanRptkModel.getRptk().getKuartal1() + ""
                + pengajuanRptkModel.getRptk().getTahun1() + "-" + pengajuanRptkModel.getRptk().getKuartal2() + ""
                + pengajuanRptkModel.getRptk().getTahun2());
        pengajuanRptkModel.setNoPengajuan(this.GenerateNoPengajuan());
        long id = (long) pengajuanService.save(pengajuanRptkModel);
        if (rwf == null) {
            rwf = new RptkWorkflow(Util.getInitialParam("workflowServerUrl"),
                    Util.getInitialParam("workflowServerQuery"),
                    Util.getInitialParam("workflowServerTaskImplementation"));
        }

        System.out.println("idnya muncul engga" + id);

        try {

            pengajuanRptkModel.setIdPengajuan(id);
            pengajuanRptkModel = (PengajuanRptk) rwf.submitTask(pengajuanRptkModel, authBean.getSession().getUsername(),
                    Action.Pengajuan, pengajuanService);

        } catch (Exception e) {
            System.out.println("[ERROR]\nPengajuan RPTK : " + e.getMessage());
            //this.viewAll();
        }

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        email.sendEmailPengajuanRptkBaru("Baru",
                pengajuanRptkModel.getPeriode(),
                pengajuanRptkModel.getRptk().getVersi(),
                pengajuanRptkModel.getNoPengajuan(),
                pengajuanRptkModel.getNoSurat(),
                pengajuanRptkModel.getPerihalSurat(),
                pengajuanRptkModel.getKkks(),
                authBean.getSession().getUsername(),
                df.format(new Date()));

        RptkManipulator manipulator = new RptkManipulator();
        System.out.println("RPTK baru sudah dibuat! ID : " + manipulator.perbaikiRptk(pengajuanRptkModel.getRptk().getId()));
        System.out.println("this id = " + id);
        this.viewAll();
    }

    public void submitAndUpdate(long id) {
        try {
            revisiRptkModel = ((PengajuanRptk) pengajuanService.getById(id));
            if (authBean.getSession().getIdkkks() != null) {
                Rptk rptk = rptkService.latestRptkByKkksId(authBean.getSession().getIdkkks());
                if (rptk != null) {
                    rptk.setKkks(null);
                    rptk.setId(rptk.getIdRptk());
                    revisiRptkModel.setRptk(rptk);
                }
            }

            revisiRptkModel.getRptk().setId_rptk(revisiRptkModel.getRptk().getId());
            revisiRptkModel.setIdRptk(revisiRptkModel.getRptk().getId());
            revisiRptkModel.setParentRptk(null);
            revisiRptkModel.getRptk().setIdRptk(revisiRptkModel.getRptk().getId_rptk());
            revisiRptkModel.setStatus("Diajukan");
            noRevisiBaru = GenerateNoPengajuanPerbaikan(revisiRptkModel.getNoPengajuan());
            revisiRptkModel.setPeriode(revisiRptkModel.getRptk().getKuartal1() + ""
                    + revisiRptkModel.getRptk().getTahun1() + "-" + revisiRptkModel.getRptk().getKuartal2() + ""
                    + revisiRptkModel.getRptk().getTahun2());

            System.out.println("no surat =" + this.revisiRptkModel.getNoSurat());
            if (rwf == null) {
                rwf = new RptkWorkflow(Util.getInitialParam("workflowServerUrl"),
                        Util.getInitialParam("workflowServerQuery"),
                        Util.getInitialParam("workflowServerTaskImplementation"));
            }
            try {
                PengajuanRptk rptkPengajuan = new PengajuanRptk();
                rptkPengajuan = revisiRptkModel;
                String temp = GenerateNoPengajuanPerbaikan(this.getRevisiRptkModel().getNoPengajuan());
                rptkPengajuan.setType("Lanjutan");
                rptkPengajuan.setNoPengajuan(temp);
                rptkPengajuan.setSuratPernyataan(null);
                rptkPengajuan.setRekomendasiDitjenMigas(null);
                rptkPengajuan.setRekomendasiKemenakertrans(null);
                rptkPengajuan.setSuratRekomendasi(null);
                rptkPengajuan.setSuratPersetujuan(null);

                rptkPengajuan = (PengajuanRptk) rwf.submitTask(rptkPengajuan, authBean.getSession().getUsername(),
                        Action.Revisi, pengajuanService);
                System.out.println("id pengajuannya = " + rptkPengajuan.getIdPengajuan());

                pengajuanService.generatePosisi(revisiRptkModel.getIdRptk(), rptkPengajuan.getIdRptk());

                this.noRevisiBaru = rptkPengajuan.getNoPengajuan();
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                email.sendEmailPengajuanRptkBaru("Lanjutan",
                        rptkPengajuan.getPeriode(),
                        revisiRptkModel.getRptk().getVersi(),
                        rptkPengajuan.getNoPengajuan(),
                        rptkPengajuan.getNoSurat(),
                        rptkPengajuan.getPerihalSurat(),
                        rptkPengajuan.getKkks(),
                        authBean.getSession().getUsername(),
                        df.format(new Date()));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('suksesDialog').show();");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void saveSubmitUpdate() {
        try {
            // setAllId();

//			revisiRptkModel.setIdPengajuan(0);
            revisiRptkModel.setSuratPengajuan(pengajuanRptkModel.getSuratPengajuan());
            revisiRptkModel.getRptk().setId_rptk(revisiRptkModel.getRptk().getId());
            revisiRptkModel.setIdRptk(revisiRptkModel.getRptk().getId());
            revisiRptkModel.setParentRptk(null);
            revisiRptkModel.getRptk().setIdRptk(revisiRptkModel.getRptk().getId_rptk());
            //revisiRptkModel.setId(revisiRptkModel.getRptk().getId_rptk());
            revisiRptkModel.setStatus("Diajukan");

            System.out.println("no surat =" + this.revisiRptkModel.getNoSurat());

            // this.getRevisiRptkModel().setNoPengajuan(temp);
            // this.getRevisiRptkModel().setStatus("Diajukan");
            // this.getRevisiRptkModel().setOleh("KKKS");
            // long id = (long) pengajuanService.revisi(revisiRptkModel);
            if (rwf == null) {
                rwf = new RptkWorkflow(Util.getInitialParam("workflowServerUrl"),
                        Util.getInitialParam("workflowServerQuery"),
                        Util.getInitialParam("workflowServerTaskImplementation"));
            }
            try {
                PengajuanRptk rptkPengajuan = new PengajuanRptk();
                rptkPengajuan = revisiRptkModel;
                String temp = GenerateNoPengajuanPerbaikan(this.getRevisiRptkModel().getNoPengajuan());
                rptkPengajuan.setType("Lanjutan");
                rptkPengajuan.setNoPengajuan(temp);
                rptkPengajuan.setSuratPernyataan(null);
                rptkPengajuan.setRekomendasiDitjenMigas(null);
                rptkPengajuan.setRekomendasiKemenakertrans(null);
                rptkPengajuan.setSuratRekomendasi(null);
                rptkPengajuan.setSuratPersetujuan(null);

                rptkPengajuan = (PengajuanRptk) rwf.submitTask(rptkPengajuan, authBean.getSession().getUsername(),
                        Action.Revisi, pengajuanService);
                System.out.println("id pengajuannya = " + rptkPengajuan.getIdPengajuan());

                this.noRevisiBaru = rptkPengajuan.getNoPengajuan();
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                email.sendEmailPengajuanRptkBaru("Lanjutan",
                        rptkPengajuan.getPeriode(),
                        revisiRptkModel.getRptk().getVersi(),
                        rptkPengajuan.getNoPengajuan(),
                        rptkPengajuan.getNoSurat(),
                        rptkPengajuan.getPerihalSurat(),
                        rptkPengajuan.getKkks(),
                        authBean.getSession().getUsername(),
                        df.format(new Date()));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('suksesDialog').show();");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void submitUpdateOrRevisi() {
        if (revisiRptkModel.getStatus().equals("Disetujui Final (Lengkap)")) {
            saveRevisi();
        } else {
            saveSubmitUpdate();
        }
    }

    public void saveRevisi() {
        try {
            // setAllId();

//			revisiRptkModel.setIdPengajuan(0);
            revisiRptkModel.setSuratPengajuan(pengajuanRptkModel.getSuratPengajuan());
            revisiRptkModel.getRptk().setId_rptk(revisiRptkModel.getRptk().getId());
            revisiRptkModel.setIdRptk(revisiRptkModel.getRptk().getId());
            revisiRptkModel.setParentRptk(null);
            revisiRptkModel.getRptk().setIdRptk(revisiRptkModel.getRptk().getId_rptk());
            //revisiRptkModel.setId(revisiRptkModel.getRptk().getId_rptk());
            revisiRptkModel.setStatus("Diajukan");
            RptkManipulator manipulator = new RptkManipulator();
            manipulator.revisiRptk(revisiRptkModel.getRptk().getId());
            System.out.println("no surat =" + this.revisiRptkModel.getNoSurat());

            // this.getRevisiRptkModel().setNoPengajuan(temp);
            // this.getRevisiRptkModel().setStatus("Diajukan");
            // this.getRevisiRptkModel().setOleh("KKKS");
            // long id = (long) pengajuanService.revisi(revisiRptkModel);
            if (rwf == null) {
                rwf = new RptkWorkflow(Util.getInitialParam("workflowServerUrl"),
                        Util.getInitialParam("workflowServerQuery"),
                        Util.getInitialParam("workflowServerTaskImplementation"));
            }
            try {
                PengajuanRptk rptkPengajuan = new PengajuanRptk();
                rptkPengajuan = revisiRptkModel;
                String temp = GenerateNoPengajuanRevisi(this.getRevisiRptkModel().getNoPengajuan());
                rptkPengajuan.setType("Revisi");
                rptkPengajuan.setNoPengajuan(temp);
                rptkPengajuan.setSuratPernyataan(null);
                rptkPengajuan.setRekomendasiDitjenMigas(null);
                rptkPengajuan.setRekomendasiKemenakertrans(null);
                rptkPengajuan.setSuratRekomendasi(null);
                rptkPengajuan.setSuratPersetujuan(null);

                rptkPengajuan = (PengajuanRptk) rwf.submitTask(rptkPengajuan, authBean.getSession().getUsername(),
                        Action.Revisi, pengajuanService);
                System.out.println("id pengajuannya = " + rptkPengajuan.getIdPengajuan());

                this.noRevisiBaru = rptkPengajuan.getNoPengajuan();
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                email.sendEmailPengajuanRptkBaru("Revisi",
                        rptkPengajuan.getPeriode(),
                        revisiRptkModel.getRptk().getVersi(),
                        rptkPengajuan.getNoPengajuan(),
                        rptkPengajuan.getNoSurat(),
                        rptkPengajuan.getPerihalSurat(),
                        rptkPengajuan.getKkks(),
                        authBean.getSession().getUsername(),
                        df.format(new Date()));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('suksesDialog').show();");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public String getEventPopUpDisplay() {
        if ("1".equals(this.eventPopUpDisplay)) {
            return "display:block";
        } else {
            return "display:none";
        }

    }

    public void setEventPopUpDisplay(String eventPopUpDisplay) {

        this.eventPopUpDisplay = eventPopUpDisplay;

    }

    public String getRenameFile() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sessionMap = externalContext.getSessionMap();
        switch (this.renameFile) {
            case "1":
                sessionMap.put("okAction", "1");
                return "surat_pengajuan_IMTA_";
            case "2":
                sessionMap.put("okAction", "2");
                return "passport_IMTA_";
            case "3":
                sessionMap.put("okAction", "3");
                return "surat_penunjukan_IMTA_";
            case "4":
                sessionMap.put("okAction", "4");
                return "performance_contract_IMTA_";
            case "5":
                sessionMap.put("okAction", "5");
                return "komitmen1_IMTA_";
            case "6":
                sessionMap.put("okAction", "6");
                return "rencana_mentoring_IMTA_";
            case "7":
                sessionMap.put("okAction", "7");
                return "hasil_mentoring_tahun_sebelumnya_IMTA_";
            case "8":
                sessionMap.put("okAction", "8");
                return "sertifikat_cross_culture_IMTA_";
            case "9":
                sessionMap.put("okAction", "9");
                return "kursus_bahasa_Indonesia_IMTA_";
            case "10":
                sessionMap.put("okAction", "10");
                return "bukti_komitmen_IMTA_";
            case "11":
                sessionMap.put("okAction", "11");
                return "AFE_IMTA_";
            case "12":
                sessionMap.put("okAction", "12");
                return "WPnB_IMTA_";
            case "13":
                sessionMap.put("okAction", "13");
                return "Surat_basah_RPTK_";
            case "14":
                sessionMap.put("okAction", "14");
                return "Surat_Basah_IMTA_";
        }
        return null;
    }

    public void setRenameFile(String renameFile) {
        this.renameFile = renameFile;
    }

    public void destroySessionTemp() {
        ExternalContext externalContext = FacesContext.getCurrentInstance()
                .getExternalContext();
        Map<String, Object> sessionMap = externalContext.getSessionMap();
        sessionMap.put("tempPathUrl", "");
        sessionMap.put("tempPathUrlDoc", "");
        sessionMap.put("tempFileName", "");
        sessionMap.put("getEventPopUpDisplay", "");
        sessionMap.put("okAction", "");

    }

    public void destroySessionTempFileName() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sessionMap = externalContext.getSessionMap();
        sessionMap.put("tempFileNameOk", "");

    }

    public void deleteTempFile() {
        try {
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            Map<String, Object> sessionMap = externalContext.getSessionMap();
            String fileTemp = sessionMap.get("tempPathUrl").toString();

            File filesd = new File(fileTemp);

            if (filesd.delete()) {
                System.out.println(filesd.getName() + " is deleted!");
            } else {
                System.out.println("Delete operation is failed.");
            }
            this.destroySessionTemp();
            this.destroySessionTempFileName();
        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    public void previewOKAction() {
        System.out.println(viewSurat.getUploadedNamaFile().getFileName());
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sessionMap = externalContext.getSessionMap();
        String tempFileName = sessionMap.get("tempFileName").toString();
        sessionMap.put("tempFileNameOk", tempFileName);
        System.out.println("submitPreviewOk:" + tempFileName);
        sessionMap.put("getEventPopUpDisplay", this.getEventPopUpDisplay());
        UploadResponse response = pengajuanService.uploadFile(viewSurat.getInputStream(), viewSurat.getContentType(), tempFileName); //upload to alfresco

        System.out.println(new Gson().toJson(response));
        if (response.getResponse() == 200) {
            pengajuanRptkModel.setSuratPengajuan(response.getResult());
            revisiRptkModel.setSuratPengajuan(response.getResult());
            perbaikanRptkModel.setSuratPengajuan(response.getResult());
            System.out.println("Berhasil");
//            Util.redirectToPage(basemodule + "/add.xhtml");
        } else {
            System.out.println("Tidak Berhasil");
        }
        this.destroySessionTemp();
        //  Util.redirectToPage(basemodule+"/add.xhtml");
    }

    public void setUploadFileEvent(FileUploadEvent event) {
        this.setEventPopUpDisplay("1");
        try {
            viewSurat.setInputStream(event.getFile().getInputstream());
        } catch (IOException ex) {
            Logger.getLogger(PengajuanRptkBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        viewSurat.setUploadedNamaFile(event.getFile());
        viewSurat.setContentType(event.getFile().getContentType());
        System.out.println(viewSurat.getUploadedNamaFile().getFileName());
        String renameRptk = "surat_pengajuan_RPTK_";
        this.uploadFilePreview(event, renameRptk);
    }

    public void setUploadFileEventRevisi(FileUploadEvent event) {
        this.setEventPopUpDisplay("1");
        try {
            viewSurat.setInputStream(event.getFile().getInputstream());
        } catch (IOException ex) {
            Logger.getLogger(PengajuanRptkBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        viewSurat.setUploadedNamaFile(event.getFile());
        viewSurat.setContentType(event.getFile().getContentType());
        System.out.println(viewSurat.getUploadedNamaFile().getFileName());
        String renameRptk = "revisi_surat_pengajuan_RPTK_";
        this.uploadFilePreview(event, renameRptk);
    }

    public void setUploadFileEventPerbaikan(FileUploadEvent event) {
        this.setEventPopUpDisplay("1");
        try {
            viewSurat.setInputStream(event.getFile().getInputstream());
        } catch (IOException ex) {
            Logger.getLogger(PengajuanRptkBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        viewSurat.setUploadedNamaFile(event.getFile());
        viewSurat.setContentType(event.getFile().getContentType());
        System.out.println(viewSurat.getUploadedNamaFile().getFileName());
        String renameRptk = "perbaikan_surat_pengajuan_RPTK_";
        this.uploadFilePreview(event, renameRptk);
    }

    public void uploadFilePreview(FileUploadEvent event) {

        ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
        String fileUploadFolder = extContext.getRealPath("//resources//tmp_file//");
        String urlApp = extContext.getInitParameter("appUrl");
        DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss");
        Date d = new Date();
        String ext = FilenameUtils.getExtension(extContext.getRealPath("//resources//tmp_file//" + event.getFile().getFileName()));
        String fileNewName = this.getRenameFile() + dateFormat.format(d) + "." + ext;
        File file = new File(fileUploadFolder);
        boolean b = false;
        if (!file.exists()) {
            b = file.mkdirs();
        }
        if (b) {
            System.out.println("Directory is created!");
        } else {
            System.out.println("Failed to create directory!");
        }
        File result = new File(extContext.getRealPath("//resources//tmp_file//" + event.getFile().getFileName()));
        File newResult = new File(extContext.getRealPath("//resources//tmp_file//" + fileNewName));
        result.renameTo(newResult);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(newResult);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bulk;
            InputStream inputStream = event.getFile().getInputstream();
            while (true) {
                bulk = inputStream.read(buffer);
                if (bulk < 0) {
                    break;
                }
                fileOutputStream.write(buffer, 0, bulk);
                fileOutputStream.flush();
            }

            fileOutputStream.close();
            inputStream.close();

            Map<String, Object> sessionMap = extContext.getSessionMap();
            sessionMap.put("tempPathUrl", urlApp + "//resources//tmp_file//" + fileNewName);
            sessionMap.put("tempPathUrlDoc", urlApp + "//resources//tmp_file//" + fileNewName + "&embedded=true");
            sessionMap.put("contentType", event.getFile().getContentType());
            sessionMap.put("tempFileName", fileNewName);
            System.out.println("FILE NEW NAME : " + sessionMap.get("tempFileName"));

            this.setEventPopUpDisplay("1");
            System.out.println("setelah di upload:" + this.getEventPopUpDisplay());
            sessionMap.put("getEventPopUpDisplay", this.getEventPopUpDisplay());
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public void uploadFilePreview(FileUploadEvent event, String renameFile) {

        ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
        String fileUploadFolder = extContext.getRealPath("//resources//tmp_file//");
        String urlApp = extContext.getInitParameter("appUrl");
        DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss");
        Date d = new Date();
        String ext = FilenameUtils.getExtension(extContext.getRealPath("//resources//tmp_file//" + event.getFile().getFileName()));
        String fileNewName = renameFile + dateFormat.format(d) + "." + ext;
        File file = new File(fileUploadFolder);
        boolean b = false;
        if (!file.exists()) {
            b = file.mkdirs();
        }
        if (b) {
            System.out.println("Directory is created!");
        } else {
            System.out.println("Failed to create directory!");
        }
        File result = new File(extContext.getRealPath("//resources//tmp_file//" + event.getFile().getFileName()));
        File newResult = new File(extContext.getRealPath("//resources//tmp_file//" + fileNewName));
        result.renameTo(newResult);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(newResult);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bulk;
            InputStream inputStream = event.getFile().getInputstream();
            while (true) {
                bulk = inputStream.read(buffer);
                if (bulk < 0) {
                    break;
                }
                fileOutputStream.write(buffer, 0, bulk);
                fileOutputStream.flush();
            }

            fileOutputStream.close();
            inputStream.close();

            Map<String, Object> sessionMap = extContext.getSessionMap();
            sessionMap.put("tempPathUrl", urlApp + "//resources//tmp_file//" + fileNewName);
            sessionMap.put("tempPathUrlDoc", urlApp + "//resources//tmp_file//" + fileNewName + "&embedded=true");
            sessionMap.put("contentType", event.getFile().getContentType());
            sessionMap.put("tempFileName", fileNewName);

            this.setEventPopUpDisplay("1");
            System.out.println("setelah di upload:" + this.getEventPopUpDisplay());
            sessionMap.put("getEventPopUpDisplay", this.getEventPopUpDisplay());
        } catch (IOException e) {
            e.printStackTrace();

        }
        System.out.println(viewSurat.getUploadedNamaFile().getFileName());
    }

    public void initEdit() {
        revisiRptkModel = ((PengajuanRptk) pengajuanService.getById(id_pengajuan));
        perbaikanRptkModel = ((PengajuanRptk) pengajuanService.getById(id_pengajuan));

        perbaikanRptkModel.setNoSurat("");
        perbaikanRptkModel.setTglSurat(null);
        perbaikanRptkModel.setPerihalSurat("");

        revisiRptkModel.setNoSurat("");
        revisiRptkModel.setTglSurat(null);
        revisiRptkModel.setPerihalSurat("");
        revisiRptkModel.setSuratPermohonan("");

        if (authBean.getSession().getIdkkks() != null) {
            Rptk rptk = rptkService.latestRptkByKkksId(authBean.getSession().getIdkkks());
            if (rptk != null) {
                rptk.setKkks(null);
                rptk.setId(rptk.getIdRptk());
                perbaikanRptkModel.setRptk(rptk);
                revisiRptkModel.setRptk(rptk);
            }
        }
        perbaikanRptkModel.setPeriode(perbaikanRptkModel.getRptk().getKuartal1() + ""
                + perbaikanRptkModel.getRptk().getTahun1() + "-" + perbaikanRptkModel.getRptk().getKuartal2() + ""
                + perbaikanRptkModel.getRptk().getTahun2());

        revisiRptkModel.setPeriode(revisiRptkModel.getRptk().getKuartal1() + ""
                + revisiRptkModel.getRptk().getTahun1() + "-" + revisiRptkModel.getRptk().getKuartal2() + ""
                + revisiRptkModel.getRptk().getTahun2());

        noRevisiBaru = GenerateNoPengajuanRevisi(revisiRptkModel.getNoPengajuan());
        // noPengajuanBaru =
        // GenerateNoPengajuanPerbaikan(perbaikanRptkModel.getNoPengajuan());
        historyKomentar = "";
        listHistoryKomentar = (ArrayList<RptkKomentar>) rptkKomentarService.getKomentarForKKKS(this.id_pengajuan, Position.Kkks, Position.Staff);
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        for (RptkKomentar listHistoryKomentar1 : listHistoryKomentar) {
            historyKomentar += df.format(listHistoryKomentar1.getUpdatedDate()) + " (" + listHistoryKomentar1.getOleh() + ") " + listHistoryKomentar1.getKonten() + "\n";
        }
    }

    public void savePerbaikan() {
        try {
            setAllId();

            System.out.println("no surat =" + this.perbaikanRptkModel.getNoSurat());
            String temp = GenerateNoPengajuanPerbaikan(this.getPerbaikanRptkModel().getNoPengajuan());
            this.getPerbaikanRptkModel().setType("Perbaikan");
            this.getPerbaikanRptkModel().setNoPengajuan(temp);
            this.getPerbaikanRptkModel().setStatus("Diajukan");
            this.getPerbaikanRptkModel().setOleh("KKKS");
            long id = (long) pengajuanService.save(perbaikanRptkModel);
            System.out.println("id nya = " + id);
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('suksesDialog').show();");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // private WorkflowManager wm;
    public void savePerbaikan2() {
        try {
            RptkManipulator manipulator = new RptkManipulator();
            manipulator.perbaikiRptk(perbaikanRptkModel.getRptk().getId());
//			perbaikanRptkModel.setIdPengajuan(0);
//			perbaikanRptkModel.getRptk().setId(perbaikanRptkModel.getRptk().getId_rptk());
//			perbaikanRptkModel.setIdRptk(perbaikanRptkModel.getRptk().getId_rptk());
//			perbaikanRptkModel.getRptk().setIdRptk(perbaikanRptkModel.getRptk().getId_rptk());
            System.out.println("no surat =" + this.perbaikanRptkModel.getNoSurat());
            // String temp =
            // GenerateNoPengajuanPerbaikan(this.getPerbaikanRptkModel().getNoPengajuan());
            // this.getPerbaikanRptkModel().setNoPengajuan(temp);
            // this.getPerbaikanRptkModel().setStatus("Diajukan");
            // this.getPerbaikanRptkModel().setOleh("KKKS");
            // long id = (long) pengajuanService.perbaikan(perbaikanRptkModel);

            // System.out.println("id nya = " + id);
            // noPengajuanBaru = temp;
            //
            // System.out.println("id nya = " + id);
            // RequestContext context = RequestContext.getCurrentInstance();
            // context.execute("PF('suksesDialog').show();");

            /* start workflow */
            if (rwf == null) {
                rwf = new RptkWorkflow(Util.getInitialParam("workflowServerUrl"),
                        Util.getInitialParam("workflowServerQuery"),
                        Util.getInitialParam("workflowServerTaskImplementation"));
            }
            //Hal ini dilakukan karena surat pengajuan di perbaikanRptkModel isinya null, padahal sudah di set saat upload.
            //Tetapi di pengajuanRptkModel surat pengajuannya tidak null.
            perbaikanRptkModel.setSuratPengajuan(pengajuanRptkModel.getSuratPengajuan());
            //end
            pengajuanRptkModel = perbaikanRptkModel;
            // pengajuanRptkModel.setIdPengajuan(id);
            // TableType.ambil_tiket kudu diganti ku Action.ambil_tiket
            try {
                String temp2 = GenerateNoPengajuanPerbaikan(this.getPerbaikanRptkModel().getNoPengajuan());
                pengajuanRptkModel.setNoPengajuan(temp2);
                pengajuanRptkModel.setType("Perbaikan");
                pengajuanRptkModel.setParentRptk((long) this.id_pengajuan);
                pengajuanRptkModel.setSuratPernyataan(null);
                pengajuanRptkModel.setRekomendasiDitjenMigas(null);
                pengajuanRptkModel.setRekomendasiKemenakertrans(null);
                pengajuanRptkModel.setSuratRekomendasi(null);
                pengajuanRptkModel.setSuratPersetujuan(null);
                pengajuanRptkModel = (PengajuanRptk) rwf.submitTask(pengajuanRptkModel,
                        authBean.getSession().getUsername(), Action.Pengajuan, pengajuanService);

                long id = pengajuanRptkModel.getIdPengajuan();
                System.out.println("id pengajuannya = " + pengajuanRptkModel.getIdPengajuan());

                this.noPengajuanBaru = pengajuanRptkModel.getNoPengajuan();

                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                email.sendEmailPengajuanRptkBaru("Perbaikan",
                        pengajuanRptkModel.getPeriode(),
                        perbaikanRptkModel.getRptk().getVersi(),
                        pengajuanRptkModel.getNoPengajuan(),
                        pengajuanRptkModel.getNoSurat(),
                        pengajuanRptkModel.getPerihalSurat(),
                        pengajuanRptkModel.getKkks(),
                        authBean.getSession().getUsername(),
                        df.format(new Date()));

                RptkKomentar rptkKomentar = new RptkKomentar();
                rptkKomentar.setKonten(getKomentar());
                rptkKomentar.setOleh(Position.Kkks);
                rptkKomentar.setPenerima(Position.Staff);
                //rptkKomentar.setStatus("0");
                rptkKomentar.setId(id);

                System.out.println("getKontenSurat=" + rptkKomentar.getKonten());
                System.out.println("get id=" + rptkKomentar.getId());
                rptkKomentarService.save(rptkKomentar);

                RequestContext context = RequestContext.getCurrentInstance();
                context.execute("PF('suksesDialog').show();");

                // Util.redirectToPage(basemodule + "list_pengajuan.xhtml");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            /* end workflow */

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void setAllId() {
        perbaikanRptkModel.setIdPengajuan(0);
        revisiRptkModel.getRptk().setId(revisiRptkModel.getRptk().getId_rptk());
        perbaikanRptkModel.getRptk().setId(perbaikanRptkModel.getRptk().getId_rptk());
        perbaikanRptkModel.setIdRptk(perbaikanRptkModel.getRptk().getId_rptk());
        perbaikanRptkModel.getRptk().setIdRptk(perbaikanRptkModel.getRptk().getId_rptk());

    }

    public void setAllidPerbaikan() {
        perbaikanRptkModel.setIdPengajuan(0);

    }

    public String getType() {
        return type;
    }

    public static enum Mode {

        ALPHA, ALPHANUMERIC, NUMERIC
    }

    public static String generateRandomString(int length, Mode mode) {
        StringBuilder buffer = new StringBuilder();
        String characters = "";
        switch (mode) {
            case ALPHA:
                characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
                break;
            case ALPHANUMERIC:
                characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
                break;
            case NUMERIC:
                characters = "1234567890";
                break;
        }
        int charactersLength = characters.length();
        for (int i = 0; i < length; i++) {
            double index = Math.random() * charactersLength;
            buffer.append(characters.charAt((int) index));
        }
        return buffer.toString();
    }

    private String GenerateNoPengajuan() {
        String noPengajuan = "A-";
        noPengajuan = noPengajuan + user.getId() + "-" + pengajuanRptkModel.getRptk().getTahun1() + "-001" + "-001";
        return noPengajuan;
    }

    private RUser GetKkks() {
        RUser retVal = pengajuanService.GetKkkksId();
        return retVal;
    }

    private String GenerateNoPengajuanRevisi(String noPengajuan) {
        String[] temp;
        String result;
        temp = noPengajuan.split("\\-");
        if (temp.length < 5) {
            result = GenerateNoPengajuan();
        } else {
            int tempInt = Integer.parseInt(temp[3]) + 1;
            result = String.format("%03d", tempInt);
            temp[3] = result;
            result = temp[0] + "-" + temp[1] + "-" + temp[2] + "-" + temp[3] + "-001";

        }

        return result;
    }

    private String GenerateNoPengajuanPerbaikan(String noPengajuan) {
        String[] temp;
        String result;

        temp = noPengajuan.split("\\-");
        if (temp.length < 5) {
            result = GenerateNoPengajuan();
        } else {
            int tempInt = Integer.parseInt(temp[4]) + 1;
            result = String.format("%03d", tempInt);
            temp[4] = result;
            result = temp[0] + "-" + temp[1] + "-" + temp[2] + "-" + temp[3] + "-" + temp[4];

        }

        return result;
    }

    public List<String> getListKuartal() {
        return listKuartal;
    }

    public void setListKuartal(List<String> listKuartal) {
        this.listKuartal = listKuartal;
    }

    public String getTahunPeriodeAwal() {
        return tahunPeriodeAwal;
    }

    public void setTahunPeriodeAwal(String tahunPeriodeAwal) {
        this.tahunPeriodeAwal = tahunPeriodeAwal;
    }

    public String getPeriodeAkhirKuartal() {
        return periodeAkhirKuartal;
    }

    public void setPeriodeAkhirKuartal(String periodeAkhirKuartal) {
        this.periodeAkhirKuartal = periodeAkhirKuartal;
    }

    public String getTahunPeriodeAkhir() {
        return tahunPeriodeAkhir;
    }

    public void setTahunPeriodeAkhir(String tahunPeriodeAkhir) {
        this.tahunPeriodeAkhir = tahunPeriodeAkhir;
    }

    public String getPeriodeAwalKuartal() {
        return periodeAwalKuartal;
    }

    public void setPeriodeAwalKuartal(String periodeAwalKuartal) {
        this.periodeAwalKuartal = periodeAwalKuartal;
    }

    public List<String> getListJenis() {
        return listJenis;
    }

    public void setListJenis(List<String> listJenis) {
        this.listJenis = listJenis;
    }

    public List<String> getListKkks() {
        return listKkks;
    }

    public void setListKkks(List<String> listKkks) {
        this.listKkks = listKkks;
    }

    public UploadedFile getCurrentfile() {
        return currentfile;
    }

    public void setCurrentfile(UploadedFile currentfile) {
        this.currentfile = currentfile;
    }

    public StreamedContent getToDownload() {
        return toDownload;
    }

    public void setToDownload(StreamedContent toDownload) {
        this.toDownload = toDownload;
    }

    public int getPreId() {
        return preId;
    }

    public void setPreId(int preId) {
        this.preId = preId;
    }

    public PengajuanRptk getPengajuanRptkModel() {
        return pengajuanRptkModel;
    }

    public void setPengajuanRptkModel(PengajuanRptk pengajuanRptkModel) {
        this.pengajuanRptkModel = pengajuanRptkModel;
    }

    public String getSelectedNoPengajuan() {
        return selectedNoPengajuan;
    }

    public PosisiPengajuanMockup getPosisiPengajuanModel() {
        return posisiPengajuanModel;
    }

    public void setPosisiPengajuanModel(PosisiPengajuanMockup posisiPengajuanModel) {
        this.posisiPengajuanModel = posisiPengajuanModel;
    }

    public List<PosisiPengajuanMockup> getPosisiPengajuanList() {
        return posisiPengajuanList;
    }

    public void setPosisiPengajuanList(List<PosisiPengajuanMockup> posisiPengajuanList) {
        this.posisiPengajuanList = posisiPengajuanList;
    }

    public void setSelectedNoPengajuan(String selectedNoPengajuan) {
        this.selectedNoPengajuan = selectedNoPengajuan;
    }

    public String getOptionSearch() {
        return optionSearch;
    }

    public void setModels(List<PengajuanRptk> models) {
        this.models = models;
    }

    public List<PengajuanRptk> getModels() {
        return models;
    }

    public void setOptionSearch(String optionSearch) {
        this.optionSearch = optionSearch;
    }

    public String getSearchForm() {
        return searchForm;
    }

    public void setSearchForm(String searchForm) {
        this.searchForm = searchForm;
    }

    public int getEditmode() {
        return editmode;
    }

    public void setEditmode(int editmode) {
        this.editmode = editmode;
    }

    public PengajuanRptk getRevisiRptkModel() {
        return revisiRptkModel;
    }

    public void setRevisiRptkModel(PengajuanRptk revisiRptkModel) {
        this.revisiRptkModel = revisiRptkModel;
    }

    public PengajuanRptk getPerbaikanRptkModel() {
        return perbaikanRptkModel;
    }

    public void setPerbaikanRptkModel(PengajuanRptk perbaikanRptkModel) {
        this.perbaikanRptkModel = perbaikanRptkModel;
    }

    public String getKomentar() {
        return komentar;
    }

    public void setKomentar(String komentar) {
        this.komentar = komentar;
    }

    public String getNoPengajuanBaru() {
        return noPengajuanBaru;
    }

    public void setNoPengajuanBaru(String noPengajuanBaru) {
        this.noPengajuanBaru = noPengajuanBaru;
    }

    public List<PengajuanRptk> getListPengajuanDalamProses() {

        return listPengajuanDalamProses;
    }

    public void loadDalamProses() {
        listPengajuanDalamProses = new ArrayList<PengajuanRptk>();
        if (this.models != null) {
            for (int i = 0; i < models.size(); i++) {
                if (models.get(i).getStatus() != null && (models.get(i).getStatus().contains(Status.Dalam_Proses)
                        || models.get(i).getStatus().contains(Status.Setuju)
                        || models.get(i).getStatus().contains(Status.Kembali))) {
                    listPengajuanDalamProses.add(models.get(i));
                }
            }
        }
    }

    public void loadAmbilTiket() {
        listAmbilTiket = new ArrayList<PengajuanRptk>();
        listAmbilTiket = modelAmbilTiket;
    }

    public List<PengajuanRptk> getListPengajuanDiajukan() {

        return listPengajuanDiajukan;
    }

    public void loadDiajukan() {
        listPengajuanDiajukan = new ArrayList<PengajuanRptk>();
        for (int i = 0; i < models.size(); i++) {
            if (models.get(i).getStatus() != null && models.get(i).getStatus().equals("Diajukan")) {
                listPengajuanDiajukan.add(models.get(i));
            }
        }
    }

    public void ambilTiket(int id) {
        // PengajuanRptk rptk = new PengajuanRptk();
        // rptk.setIdPengajuan(id);

        // pengajuanService.ambilTiket(rptk);
        if (rwf == null) {
            rwf = new RptkWorkflow(Util.getInitialParam("workflowServerUrl"),
                    Util.getInitialParam("workflowServerQuery"),
                    Util.getInitialParam("workflowServerTaskImplementation"));
        }
        pengajuanRptkModel = (PengajuanRptk) pengajuanService.get2(id);
        // pengajuanRptkModel.getRptk().setId(pengajuanRptkModel.getRptk().getIdRptk());
        pengajuanRptkModel.getRptk().getKkks().setIdKkks(pengajuanRptkModel.getRptk().getKkks().getId());

        // pengajuanRptkModel.setIdPengajuan(id);
        // TableType.ambil_tiket kudu diganti ku Action.ambil_tiket
        pengajuanRptkModel.setId(pengajuanRptkModel.getIdPengajuan());
        pengajuanRptkModel.getRptk().setId(pengajuanRptkModel.getRptk().getIdRptk());
        try {
            rwf.submitTask(pengajuanRptkModel, authBean.getSession().getUsername(), Action.Ambil_Tiket,
                    pengajuanService);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //CR, template, rekhas, insert template from rptk_ii_template to rptk_ii_surat
        //add here
        //get template
        List<RptkTemplate> listRptkTemplate = (ArrayList<RptkTemplate>) rptkTemplateService.listAll();
        rptkSuratList = (ArrayList<RptkSurat>) rptkSuratService.get(pengajuanRptkModel.getIdPengajuan());
        if (rptkSuratList.size() == 0) {
            for (RptkTemplate item : listRptkTemplate) {
                if (item.getNamaSurat().equals("Surat Persetujuan")) {
                    String templateAfterFilled = fillSuratPersetujuanTemplate(item.getTemplate(), pengajuanRptkModel);
                    saveRptkSurat("SuratPersetujuan", templateAfterFilled, pengajuanRptkModel.getIdPengajuan());
                }

                if (item.getNamaSurat().equals("Surat Rekomendasi")) {
                    String templateAfterFilled = fillSuratRekomendasiTemplate(item.getTemplate(), pengajuanRptkModel);
                    saveRptkSurat("SuratRekomendasi", templateAfterFilled, pengajuanRptkModel.getIdPengajuan());
                }

                if (item.getNamaSurat().equals("Formulir Persetujuan")) {
                    String templateAfterFilled = fillFormulirPersetujuanTemplate(item.getTemplate(), pengajuanRptkModel);
                    saveRptkSurat("FormPersetujuan", templateAfterFilled, pengajuanRptkModel.getIdPengajuan());
                }

            }
        }
        //
        Util.redirectToPage("/modules/dashboard/list_tiket.xhtml");

    }

    public void saveRptkSurat(String jenisSurat, String kontenSurat, long idPengajuanRptk) {
        System.out.println("==save rptk surat==");
        RptkSurat rptkSurat = new RptkSurat();
        rptkSurat.setJenisSurat(jenisSurat);
        rptkSurat.setKontenSurat(kontenSurat);

        rptkSurat.setId(idPengajuanRptk);

        rptkSuratService.save(rptkSurat);
    }

    public String fillSuratPersetujuanTemplate(String template, PengajuanRptk pengajuanRptk) {
        // TODO Auto-generated method stub
        String finalResult = template.replace("{tahun}", getStringTime("yyyy"));
        finalResult = finalResult.replace("{perihal}", null != pengajuanRptk.getPerihalSurat() ? pengajuanRptk.getPerihalSurat() : "{perihal}");
        finalResult = finalResult.replace("{nama_kkks}", null != pengajuanRptk.getKkks() ? pengajuanRptk.getKkks() : "{nama_kkks}");
        String periode = pengajuanRptk.getPeriode();
        if (null != periode || !"".equals(periode)) {
//			String kwartalAkhir = periode.substring(6, 7);
            String periodeAkhir = periode.substring(7, 11);
//			String kwartalAwal = periode.substring(0, 1);
            String periodeAwal = periode.substring(1, 5);
            finalResult = finalResult.replace("{periode}", periodeAwal + "-" + periodeAkhir);
        }

        finalResult = finalResult.replace("{no_surat}", null != pengajuanRptk.getNoSurat() ? pengajuanRptk.getNoSurat() : "{no_surat}");

        if (null != pengajuanRptk.getTglSurat()) {
            finalResult = finalResult.replace("{tanggal_surat}", new SimpleDateFormat("dd MMMM yyyy", new Locale("in", "ID")).format(pengajuanRptk.getTglSurat()));
        }

        int jmlPenambahanPosisi = rptkPosisiService.getJmlPenambahanPosisi(pengajuanRptk.getRptk().getIdRptk());
        finalResult = finalResult.replace("{jml_penambahan_posisi}", String.valueOf(jmlPenambahanPosisi));

        int jmlPerpanjangan = rptkPosisiService.getJmlPerpanjangan(pengajuanRptk.getRptk().getIdRptk());
        finalResult = finalResult.replace("{jml_perpanjangan}", String.valueOf(jmlPerpanjangan));

        int jmlPerpanjanganPerubahan = rptkPosisiService.getJmlPerpanjanganPerubahan(pengajuanRptk.getRptk().getIdRptk());
        finalResult = finalResult.replace("{jml_perpanjangan_dan_perubahan}", String.valueOf(jmlPerpanjanganPerubahan));

        int jmlBelumMenyetujui = rptkPosisiService.getJmlBelumMenyetujui(pengajuanRptk.getRptk().getIdRptk());
        finalResult = finalResult.replace("{jml_belum_menyetujui}", String.valueOf(jmlBelumMenyetujui));

        return finalResult;
    }

    public String fillSuratRekomendasiTemplate(String template, PengajuanRptk pengajuanRptk) {
        // TODO Auto-generated method stub
        String finalResult = template.replace("{tahun}", getStringTime("yyyy"));
        finalResult = finalResult.replace("{perihal}", null != pengajuanRptk.getPerihalSurat() ? pengajuanRptk.getPerihalSurat() : "{perihal}");
        finalResult = finalResult.replace("{nama_kkks}", null != pengajuanRptk.getKkks() ? pengajuanRptk.getKkks() : "{nama_kkks}");

        String periode = pengajuanRptk.getPeriode();
        if (null != periode || !"".equals(periode)) {
//			String kwartalAkhir = periode.substring(6, 7);
            String periodeAkhir = periode.substring(7, 11);
//			String kwartalAwal = periode.substring(0, 1);
            String periodeAwal = periode.substring(1, 5);
            finalResult = finalResult.replace("{periode}", periodeAwal + "-" + periodeAkhir);
        }
        finalResult = finalResult.replace("{no_surat}", null != pengajuanRptk.getNoSurat() ? pengajuanRptk.getNoSurat() : "{no_surat}");
        if (null != pengajuanRptk.getTglSurat()) {
            finalResult = finalResult.replace("{tanggal_surat}", new SimpleDateFormat("dd MMMM yyyy", new Locale("in", "ID")).format(pengajuanRptk.getTglSurat()));
        }

        int jmlPenambahanPosisi = rptkPosisiService.getJmlPenambahanPosisi(pengajuanRptk.getRptk().getIdRptk());
        finalResult = finalResult.replace("{jml_penambahan_posisi}", String.valueOf(jmlPenambahanPosisi));

        int jmlPerpanjangan = rptkPosisiService.getJmlPerpanjangan(pengajuanRptk.getRptk().getIdRptk());
        finalResult = finalResult.replace("{jml_perpanjangan}", String.valueOf(jmlPerpanjangan));

        int jmlPerpanjanganPerubahan = rptkPosisiService.getJmlPerpanjanganPerubahan(pengajuanRptk.getRptk().getIdRptk());
        finalResult = finalResult.replace("{jml_perpanjangan_dan_perubahan}", String.valueOf(jmlPerpanjanganPerubahan));

        int jmlBelumMenyetujui = rptkPosisiService.getJmlBelumMenyetujui(pengajuanRptk.getRptk().getIdRptk());
        finalResult = finalResult.replace("{jml_belum_menyetujui}", String.valueOf(jmlBelumMenyetujui));

        return finalResult;
    }

    public String fillFormulirPersetujuanTemplate(String template, PengajuanRptk pengajuanRptk) {
        // TODO Auto-generated method stub
        String finalResult = template.replace("{tahun}", getStringTime("yyyy"));
        finalResult = finalResult.replace("{perihal}", null != pengajuanRptk.getPerihalSurat() ? pengajuanRptk.getPerihalSurat() : "{perihal}");
        finalResult = finalResult.replace("{nama_kkks}", null != pengajuanRptk.getKkks() ? pengajuanRptk.getKkks() : "{nama_kkks}");
        String periode = pengajuanRptk.getPeriode();
        if (null != periode || !"".equals(periode)) {
//			String kwartalAkhir = periode.substring(6, 7);
            String periodeAkhir = periode.substring(7, 11);
//			String kwartalAwal = periode.substring(0, 1);
            String periodeAwal = periode.substring(1, 5);
            finalResult = finalResult.replace("{periode}", periodeAwal + "-" + periodeAkhir);
        }
        finalResult = finalResult.replace("{no_surat}", null != pengajuanRptk.getNoSurat() ? pengajuanRptk.getNoSurat() : "{no_surat}");
        if (null != pengajuanRptk.getTglSurat()) {
            finalResult = finalResult.replace("{tanggal_surat}", new SimpleDateFormat("dd MMMM yyyy", new Locale("in", "ID")).format(pengajuanRptk.getTglSurat()));
        }

        return finalResult;
    }

    public String getStringTime(String format) {
        String timeStamp = new SimpleDateFormat(format, new Locale("in", "ID")).format(Calendar.getInstance().getTime());
        return timeStamp;
    }

    public void setAuthBean(AuthBean authBean) {
        this.authBean = authBean;
    }

    public String getNoRevisiBaru() {
        return noRevisiBaru;
    }

    public void setNoRevisiBaru(String noRevisiBaru) {
        this.noRevisiBaru = noRevisiBaru;
    }

    public ArrayList<RptkKomentar> getListHistoryKomentar() {
        return listHistoryKomentar;
    }

    public void setListHistoryKomentar(ArrayList<RptkKomentar> listHistoryKomentar) {
        this.listHistoryKomentar = listHistoryKomentar;
    }

    public String getHistoryKomentar() {
        return historyKomentar;
    }

    public void setHistoryKomentar(String historyKomentar) {
        this.historyKomentar = historyKomentar;
    }

    public List<PengajuanRptk> getModelRiwayat() {
        return modelRiwayat;
    }

    public void setModelRiwayat(List<PengajuanRptk> modelRiwayat) {
        this.modelRiwayat = modelRiwayat;
    }

    public List<PengajuanRptk> getFilteredModelRiwayat() {
        return filteredModelRiwayat;
    }

    public void setFilteredModelRiwayat(List<PengajuanRptk> filteredModelRiwayat) {
        this.filteredModelRiwayat = filteredModelRiwayat;
    }

    public String getCurentRole() {
        return authBean.olehRole();
    }

    public String statusMask(String stat) {
        if (stat.equals(Status.Setuju) || stat.equals(Status.Kembali)) {
            return Status.Dalam_Proses;
        } else {
            return stat;
        }
    }

    public void goToDetailRptk() {
        Util.redirectToPage("/modules/rptk/pengajuan/detail.xhtml");
    }

}
