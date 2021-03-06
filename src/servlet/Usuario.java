package servlet;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
//import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.bind.DatatypeConverter;

//import org.apache.commons.fileupload.FileItem;
//import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
//import org.apache.tomcat.util.buf.UDecoder;
import org.apache.tomcat.util.codec.binary.Base64;

import beans.BeanCursoJsp;
import dao.DaoUsuario;


/**
 * Servlet implementation class Usuario
 */
@MultipartConfig       // ESSA NOTA��O PERMITE ENVIAR DADOS EM TEXTO E UPLOADS PARA O BANCO
@WebServlet("/salvarUsuario" )
public class Usuario extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private DaoUsuario daoUsuario = new DaoUsuario();
       
   
    public Usuario() {
        super();
        
    }

	                     // PARA EXCLUIR:
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		try {
		String acao = request.getParameter("acao");
		String user = request.getParameter("user");
		if(acao != null && acao.equalsIgnoreCase("delete") && user != null) {
			daoUsuario.delete(user);
      RequestDispatcher view = request.getRequestDispatcher("/cadastroUsuario.jsp");
			request.setAttribute("usuarios",  daoUsuario.listar());
			view.forward(request, response);
		}        
		                // AQUI PARA EDITAR USUARIO:
		
		else if (acao != null && acao.equalsIgnoreCase("editar")) {
			BeanCursoJsp beanCursoJsp = daoUsuario.consultar(user);
			RequestDispatcher view = request.getRequestDispatcher("/cadastroUsuario.jsp");
			request.setAttribute("user",  beanCursoJsp);
			view.forward(request, response);
			
		}else if (acao != null && acao.equalsIgnoreCase("listartodos")) {
			RequestDispatcher view = request.getRequestDispatcher("/cadastroUsuario.jsp");
			request.setAttribute("usuarios",  daoUsuario.listar());
			view.forward(request, response);

		}else if (acao != null && acao.equalsIgnoreCase("download")){
			BeanCursoJsp usuario = daoUsuario.consultar(user);
			if (usuario != null) {
				
				String contentType = "";
				byte[] fileBytes = null; 
				
				String tipo = request.getParameter("tipo");
				           
				                   //IMAGEM:
				
				if (tipo.equalsIgnoreCase("imagem")){ // imagem vem do cadastroUsuario: download&tipo=imagem
					contentType = usuario.getContentType();
					fileBytes = new Base64().decodeBase64(usuario.getFotoBase64());
					
					               
					             // PDF:
					
				}else if (tipo.equalsIgnoreCase("curriculo")){ // curriculo vem do cadastroUsuario: download&tipo=curriculo
					contentType = usuario.getContentTypeCurriculo();
					fileBytes = new Base64().decodeBase64(usuario.getCurriculoBase64());
				}
				
				response.setHeader("Content-Disposition", "attachment;filename=arquivo."
			   + contentType.split("\\/")[1]);
				
				
				/*Coloca os bytes em um objeto de entrada para processar*/
				InputStream is = new ByteArrayInputStream(fileBytes);
				
				/*inicio da resposta para o navegador*/
				int read= 0;
				byte[] bytes = new byte[1024];
				OutputStream os = response.getOutputStream();
				
				
				while ((read = is.read(bytes)) != -1) {
					os.write(bytes, 0, read);
				}
				
				os.flush();
				os.close();
				
			}
		}else {
			RequestDispatcher view = request.getRequestDispatcher("/cadastroUsuario.jsp");
			request.setAttribute("usuarios",  daoUsuario.listar());
			view.forward(request, response);
			
		}
		
		
	}catch (Exception e) {
		e.printStackTrace();	
	}
	}
	
	
	
	

	            // PARA INCLUIR E SALVAR USARIO NOVO:
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		//doGet(request, response);
		
		
		String acao = request.getParameter("acao");   // PARA O BOT�O CANCELAR NO EIDTAR USUARIO
		if (acao!= null && acao.equalsIgnoreCase("reset")) {
			
			
			 // PARA FICAR NA MESMA P�GINA AP�S CANCELAR EDI��O DE USUARIO:
			
			try {
				RequestDispatcher view = request.getRequestDispatcher("/cadastroUsuario.jsp");
				
				request.setAttribute("usuarios",  daoUsuario.listar());
				view.forward(request, response);
				
			}catch (Exception e) {
				e.printStackTrace();
					
			}
			
		}else {
		
		
		String id = request.getParameter("id");
		String login = request.getParameter("login");
		String senha = request.getParameter("senha");
		String nome = request.getParameter("nome");
		
		
		String cep = request.getParameter("cep");
		String rua = request.getParameter("rua");
		String bairro = request.getParameter("bairro");
		String cidade = request.getParameter("cidade");
		String estado = request.getParameter("estado");
		String ibge = request.getParameter("ibge");
		String sexo = request.getParameter("sexo");
		String perfil = request.getParameter("perfil");
		
		BeanCursoJsp usuario = new BeanCursoJsp();
		usuario.setId(!id.isEmpty() ? Long.parseLong(id) : 0);
		usuario.setLogin(login);
		usuario.setSenha(senha);
		usuario.setNome(nome);
		
		
		usuario.setCep(cep);
		usuario.setRua(rua);
		usuario.setBairro(bairro);
		usuario.setCidade(cidade);
		usuario.setEstado(estado);
		usuario.setIbge(ibge);
		usuario.setPerfil(perfil);
		usuario.setSexo(sexo);
		
		            // CHECKBOX ATIVO E INATIVO:
		if (request.getParameter("ativo") != null && request.getParameter("ativo").equalsIgnoreCase("on")) {
			usuario.setAtivo(true);
		}else {
			usuario.setAtivo(false);
		}
		
		
		
		
		
		try {
			
			/*Inicio File upload de imagems e pdf*/
			
			if (ServletFileUpload.isMultipartContent(request)){

				
				                 // PROCESSA IMAGEM:
				
				Part imagemFoto = request.getPart("foto");
				
				if (imagemFoto != null && imagemFoto.getInputStream().available() > 0) {
					
					
				
					String fotoBase64 = new Base64().encodeBase64String(converteStremParabyte(imagemFoto.getInputStream()));
					
					usuario.setFotoBase64(fotoBase64);
					usuario.setContentType(imagemFoto.getContentType());
					
					
					
					// IN�CIO MINIATURA DE IMAGEM
					
					// 1� TRANSFORMAR EM bufferedImage:
					byte[] imageByteDecode = new Base64().decodeBase64(fotoBase64);
					BufferedImage buffereImage = ImageIO.read(new ByteArrayInputStream(imageByteDecode));
					
					
					// 2� PEGA AGORA O TIPO DA IMAGEM:			
					int type = buffereImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : buffereImage.getType();
					
					
					// 3� CRIANDO A MINIATURA:
					BufferedImage resizedImage = new BufferedImage(100, 100, type);
					Graphics2D g = resizedImage.createGraphics();
					g.drawImage(buffereImage, 0, 0, 100, 100, null);
					g.dispose();
					
					
					// 4� ESCREVER A IMAGEM NOVAMENTE:
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ImageIO.write(resizedImage, "png", baos);
					
					
					String miniaturaBase64 = "data:image/png;base64," + DatatypeConverter.printBase64Binary(baos.toByteArray());
					
					usuario.setFotoBase64Miniatura(miniaturaBase64);
					
					// FIM MINIATURA DE IMAGEM
					
					
					
				}else {
					usuario.setAtualizarImage(false);
				}
				
				                             // PROCESSA PDF:
				Part curriculoPdf = request.getPart("curriculo");
				if (curriculoPdf != null && curriculoPdf.getInputStream().available() > 0){
					String curriculoBase64 = new Base64()
					.encodeBase64String(converteStremParabyte(curriculoPdf.getInputStream()));
					
					usuario.setCurriculoBase64(curriculoBase64);
					usuario.setContentTypeCurriculo(curriculoPdf.getContentType());
					}else {
						usuario.setAtualizarPdf(false);
					}
			}
			
			
			// FIM C�DIGO FILE UPLOAD DE IMAGENS E PDF
			
			
			String msg = null;
			boolean podeInserir = true;
			
			    // VALIDA��ES DE CAMPOS VAZIOS:          
			
			if(login == null || login.isEmpty()) {
				msg = "Login deve ser informado!!!";  // VALIDAR SE CAMPO LOGIN FOI PREENCHIDO OU N�O
				podeInserir = false;
			}
			
			
			else if(senha == null || senha.isEmpty()) {
				msg = "A Senha deve ser informada!!!";  // VALIDAR SE CAMPO SENHA FOI PREENCHIDO OU N�O
				podeInserir = false;
			}
			
			
			else if(nome == null || nome.isEmpty()) {
				msg = "O nome deve ser informado!!!";  // VALIDAR SE CAMPO NOME FOI PREENCHIDO OU N�O
				podeInserir = false;
			}
			
			//else if(telefone == null || telefone.isEmpty()) {
			//	msg = "O telefone deve ser informado!!!";  // VALIDAR SE CAMPO TELEFONE FOI PREENCHIDO OU N�O
			//	podeInserir = false;
			//}
			
			
			
			
			
			
			

			if (id == null || id.isEmpty()
					&& !daoUsuario.validarLogin(login)) {//QUANDO DOR USU�RIO NOVO
				msg = "Usu�rio j� existe com o mesmo login!";
				podeInserir = false;

			} else if (id == null || id.isEmpty()
					&& !daoUsuario.validarSenha(senha)) {// QUANDO FOR USU�RIO NOVO
				msg = "\n A senha j� existe para outro usu�rio!";
				podeInserir = false;
			}

			if (msg != null) {
				request.setAttribute("msg", msg);
			}

			if (id == null || id.isEmpty()
					&& daoUsuario.validarLogin(login) && podeInserir) {

				daoUsuario.salvar(usuario);

			} else if (id != null && !id.isEmpty() && podeInserir) {
				daoUsuario.atualizar(usuario);
			}
			
			if (!podeInserir){
				request.setAttribute("user", usuario);
			}

			RequestDispatcher view = request
					.getRequestDispatcher("/cadastroUsuario.jsp");
			request.setAttribute("usuarios", daoUsuario.listar());
			request.setAttribute("msg", "Usu�rio Salvo com Sucesso!!!");
			view.forward(request, response);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
	// M�TODO QUE CONVERTE A ENTRADA DE FLUXO DE DADOS DA IMAGEM P 1 ARRAY[] DE BYTE:

	private byte[] converteStremParabyte(InputStream imagem) throws Exception{
		
	 ByteArrayOutputStream baos = new ByteArrayOutputStream();
	 int reads = imagem.read();
	 while (reads != -1){
		 baos.write(reads);
		 reads = imagem.read();
	 }
	 
	 return baos.toByteArray();
	
	}

}