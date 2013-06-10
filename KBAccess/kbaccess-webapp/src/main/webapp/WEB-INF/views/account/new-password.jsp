<%@ page contentType="text/html;charset=UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html lang="fr">
    <c:set var="title" value="Profile de l'utilisateur ${account.id}" />
    <%@include file="/WEB-INF/template/head.jspf" %>

    <body>
        <%@ include file='/WEB-INF/template/header.jspf' %>
        
        <%@include file="/WEB-INF/template/breadcrumb-trail.jspf" %>
        
        <div class="page-header">
            <h1>Nouveau mot de passe</h1>
        </div>
        
        <c:choose>
            <c:when test="${not empty successMessage}">
                    <div class="row-fluid">
                        <p class="alert alert-success">${successMessage}</p>
                    </div>
            </c:when>
            <c:otherwise>
                <div class="row-fluid">
                    <spring:hasBindErrors name="newPasswordCommand">
                        <form:errors path="generalErrorMessage" cssClass="alert alert-error" element="p"/>         
                    </spring:hasBindErrors>
                        <div class="boite">
                            <form:form action="new-password.html"
                                    commandName="newPasswordCommand"
                                    method="POST"
                                    id="change-password-form"
                                    class="form-horizontal">
                                <%@include file="/WEB-INF/template/block/mandatory-fields.jspf" %>

                                <div class="control-group">
                                    <form:hidden path="token"/>
                                    <label class="control-label" for="new_password"><%@include file="/WEB-INF/template/inline/mandatory.jspf" %> Nouveau mot de passe :</label>
                                    <div class="controls">
                                        <form:password path="newPassword" id="new_password"/>
                                        <p class="label-indication">8 caractères minimum avec majuscule(s), minuscule(s) et chiffre(s)</p>
                                        <form:errors path="newPassword" cssClass="alert alert-error" element="p"/>
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="account_password_confirmation"><%@include file="/WEB-INF/template/inline/mandatory.jspf" %> Nouveau mot de passe (confirmation) :</label>
                                    <div class="controls">
                                        <form:password path="passwordConfirmation" id="account_password_confirmation"/>	
                                        <form:errors path="passwordConfirmation" cssClass="alert alert-error" element="p"/>
                                    </div>
                                </div>
                                <div class="form-actions">
                                    <button class="btn btn-primary">Changer le mot de passe</button>
                                </div>
                           </form:form>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
        <%@ include file='/WEB-INF/template/footer.jspf' %>
    </body>
</html>