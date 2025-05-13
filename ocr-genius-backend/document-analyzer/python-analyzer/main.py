from fastapi import FastAPI, File, UploadFile
from io import BytesIO
from langchain_community.llms import Ollama  # You can replace this with the model you want to use
import json

app = FastAPI()

# Initialize the LLM model (you can replace this with a suitable model for your use case)
llm = Ollama(model="llama3")  # Change model name based on your requirement

@app.post("/analyze")
async def analyze_document(file: UploadFile = File(...)):
    try:
        # Read the file
        contents = await file.read()

        # If the file is a PDF or another format that needs specific handling, you can process it here.
        # For now, let's assume the file is just plain text for simplicity
        # You can use libraries like PyPDF2 or pdfminer.six to extract text from PDFs if needed.

        document_text = contents.decode("utf-8")  # Assuming text file, change to proper extraction for other formats.

        # Process the document using the LLM model (in this case, using Ollama)
        response = llm(document_text)

        # Return the response from the LLM model as the result
        return {"message": response}

    except Exception as e:
        return {"error": str(e)}


@app.get("/")
def read_root():
    return {"message": "API Python OCR Analyzer prête 🚀"}